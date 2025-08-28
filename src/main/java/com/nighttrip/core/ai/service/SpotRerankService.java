package com.nighttrip.core.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nighttrip.core.ai.repository.TouristSpotRepositoryAi;
import com.nighttrip.core.domain.touristspot.dto.RecommendTouristSpotResponse;
import com.nighttrip.core.ai.dto.RerankCandidate;
import com.nighttrip.core.ai.dto.RerankResult;
import com.nighttrip.core.ai.dto.UserContext;
import com.nighttrip.core.ai.header.ClovaHeaders;
import com.nighttrip.core.global.enums.ImageSizeType;
import com.nighttrip.core.global.enums.ImageType;
import com.nighttrip.core.global.enums.SpotCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, transactionManager = "aiTransactionManager")
public class SpotRerankService {

    private final TouristSpotRepositoryAi aiRepo;
    private final WebClient clovaWebClient;
    private final ClovaHeaders clovaHeaders;
    private final ObjectMapper om;

    @Value("${llm.rerank.maxCandidates:40}") private int maxCandidates;
    @Value("${llm.rerank.topK:6}") private int topK;

    public RerankResult recommend(Long cityId, UserContext user) {
        var candidates = fetchAndMapCandidates(cityId, maxCandidates);

        int targetK = Optional.ofNullable(user.maxSpots())
                .filter(k -> k != null && k > 0)
                .orElse(topK);
        targetK = Math.min(targetK, candidates.size());
        if (targetK <= 0) targetK = Math.min(6, Math.max(1, candidates.size()));

        // 1) 시스템 프롬프트 (강한 제약)
        final String RERANK_SYSTEM_STRICT = """
        역할: 당신은 도시 내 관광지 추천 재랭킹 전문가다.

        목표:
        - 입력 candidates와 user 컨텍스트를 기반으로, 사용자의 시간대·스타일·동행·선호 태그를 고려해 상위 Top-K 후보지를 선정한다.
        - 출력은 **JSON 배열 하나**만 허용하며, **절대** 다른 텍스트·설명·코드펜스(```)·경고문을 붙이지 않는다.
        - JSON이 길더라도 **절대 중간에 멈추지 말고** 모든 후보를 끝까지 생성한다.

        선정 규칙(필수):
        1. night 모드 → meta.night_suitability ≥ 0.6 가점, ≤ 0.3 감점.
        2. travel_style / companions / preferences 와 meta.style / companions_fit / tags 일치 개수↑ 가점.
        3. popularityHint 높을수록 가점.
        4. 동일 카테고리 과밀 방지.
        5. meta.must_know 또는 tags에 야간 부적합 제약이 있으면 감점/제외.
        6. 최종 개수는 hints.topK 또는 user.max_spots 기준.

        출력 형식(반드시 준수):
        [
          {
            "id": number,
            "reason": "1~2문장 간결한 선정 이유",
            "score": number,   // 0.00~1.00
            "rank": number     // 1부터 시작
          },
          ...
        ]

        제약(중요):
        - JSON 배열만 출력. JSON 외 텍스트/주석/코드펜스 금지.
        - 배열 길이는 반드시 hints.topK 또는 user.max_spots를 따른다.
        - JSON 문법 준수: NaN/undefined/null 금지.
        - 출력이 길어도 끊지 말고 완전한 JSON 생성.
    """;

        // 2) user 컨텍스트 + candidates + hints(topK)
        var userJson = Map.of(
                "user", Map.of(
                        "time_mode", user.timeMode(),
                        "travel_style", user.travelStyle(),
                        "companions", user.companions(),
                        "preferences", user.preferences(),
                        "max_spots", user.maxSpots() != null ? user.maxSpots() : null
                ),
                "candidates", candidates,
                "hints", Map.of("topK", targetK)
        );

        Map<String, Object> body = null;
        try {
            body = Map.of(
                    "messages", List.of(
                            Map.of("role", "system", "content", RERANK_SYSTEM_STRICT),
                            Map.of("role", "user", "content", om.writeValueAsString(userJson))
                    ),
                    "topP", 0.8,
                    "topK", 0,
                    "maxTokens", 4096,
                    "temperature", 0.2,
                    "repetitionPenalty", 1.1,
                    "stop", List.of(),
                    "seed", 0,
                    "includeAiFilters", true
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        String resp = clovaWebClient.post()
                .uri("/v3/chat-completions/HCX-005")
                .headers(clovaHeaders.apply())
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        log.info("[RERANK] Clova 응답 원본 : {}", resp);

        RerankResult result;
        try {
            var arrJson = extractJsonArray(resp);
            result = RerankResult.fromArrayJson(arrJson, om);

            // 3) 응답 길이 강제 정합성: targetK만 남기고, route 재구성
            result = enforceTopK(result, targetK);

            log.info("[RERANK] 최종 RerankResult : {}", om.writeValueAsString(result));
        } catch (Exception e) {
            log.error("[RERANK] Clova 응답 파싱 실패", e);
            throw new RuntimeException("Rerank 응답 파싱 실패", e);
        }

        return guardRails(result, candidates);
    }

    private List<RerankCandidate> fetchAndMapCandidates(Long cityId, int max) {
        var rows = aiRepo.findCandidates(cityId, max, 0);

        return rows.stream().map(r -> {
            double popularity = normPopularity(r.mainWeight(), r.checkCount());
            var meta = parseMeta(r.computedMeta());
            String categoryStr = (r.category() == null) ? null : r.category().name();

            return new RerankCandidate(
                    r.id(),
                    r.spotName(),
                    categoryStr,
                    popularity,
                    meta
            );
        }).toList();
    }

    public List<RecommendTouristSpotResponse> toRankedDtos(RerankResult r) {
        if (r == null || r.topSpots() == null || r.topSpots().isEmpty()) return List.of();

        var idsInRankOrder = r.topSpots().stream().map(RerankResult.RankedSpot::id).toList();

        // 1쿼리로 스팟 요약 + 썸네일 URL까지
        var rows = aiRepo.findRowsWithThumbByIds(
                idsInRankOrder, ImageType.TOURIST_SPOT, ImageSizeType.THUMBNAIL);

        // id -> row 매핑 (중복 생겨도 첫 값 유지)
        Map<Long, Object[]> rowById = new HashMap<>(rows.size());
        for (Object[] row : rows) {
            Long id = (Long) row[0];
            rowById.putIfAbsent(id, row);
        }

        // rank 순으로 RecommendTouristSpotResponse 생성
        return r.topSpots().stream()
                .sorted(Comparator.comparingInt(RerankResult.RankedSpot::rank))
                .map(rs -> {
                    var row = rowById.get(rs.id());
                    if (row == null) return null;

                    Long id               = (Long)       row[0];
                    String spotName       = (String)     row[1];
                    String address        = (String)     row[2];
                    SpotCategory category = (SpotCategory) row[3];
                    String description    = (String)     row[4];
                    String thumbUrl       = (String)     row[5];

                    return new RecommendTouristSpotResponse(
                            id,
                            rs.rank(),
                            rs.reason(),
                            spotName,
                            address,
                            (category != null ? category.getKoreanName() : null),
                            description,
                            thumbUrl
                    );
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private double normPopularity(Integer main, Integer checks) {
        double m = Optional.ofNullable(main).orElse(0);
        double c = Optional.ofNullable(checks).orElse(0);
        return Math.tanh((0.7 * m + 0.3 * c) / 1000.0);
    }

    private Map<String, Object> parseMeta(String json) {
        if (json == null || json.isBlank()) return Map.of();
        try {
            return om.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("[RERANK] computedMeta 파싱 실패: {}", json);
            return Map.of();
        }
    }

    private RerankResult enforceTopK(RerankResult r, int targetK) {
        if (r == null || r.topSpots() == null) {
            return new RerankResult(List.of(), List.of(), "empty result");
        }
        var trimmed = r.topSpots().stream()
                .sorted(Comparator.comparingInt(RerankResult.RankedSpot::rank))
                .limit(targetK)
                .toList();

        var route = IntStream.range(0, trimmed.size())
                .mapToObj(i -> new RerankResult.RouteStep(i + 1, trimmed.get(i).id()))
                .toList();

        return new RerankResult(trimmed, route, r.notes());
    }

    private String extractJsonArray(String resp) {
        try {
            // 응답을 먼저 JSON으로 파싱
            JsonNode root = om.readTree(resp);
            // v3 포맷: result.message.content 에 모델 출력이 "문자열"로 옴
            String content = root.path("result").path("message").path("content").asText("");
            // content 안에서 배열만 슬라이스
            int s = content.indexOf('[');
            int e = content.lastIndexOf(']');
            if (s >= 0 && e > s) return content.substring(s, e + 1);
            // 혹시 content가 비어 있으면(예외 케이스) 기존 백업 방식
            String fallback = resp;
            s = fallback.indexOf('[');
            e = fallback.lastIndexOf(']');
            return (s >= 0 && e > s) ? fallback.substring(s, e + 1) : "[]";
        } catch (Exception e) {
            log.error("[RERANK] v3 content 추출 실패", e);
            return "[]";
        }
    }

    private RerankResult guardRails(RerankResult r, List<RerankCandidate> pool) {
        // 중복/카테고리 편중 보정 필요 시 여기 추가
        return r;
    }
}

