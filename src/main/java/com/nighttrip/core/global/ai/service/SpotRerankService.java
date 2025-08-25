package com.nighttrip.core.global.ai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nighttrip.core.domain.touristspot.repository.TouristSpotRepository;
import com.nighttrip.core.global.ai.ClovaHeaders;
import com.nighttrip.core.global.ai.dto.RerankCandidate;
import com.nighttrip.core.global.ai.dto.RerankResult;
import com.nighttrip.core.global.ai.dto.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SpotRerankService {

    private final TouristSpotRepository repo;
    private final WebClient clovaWebClient;
    private final ClovaHeaders clovaHeaders;
    private final ObjectMapper om;

    @Value("${llm.rerank.maxCandidates:40}") private int maxCandidates;
    @Value("${llm.rerank.topK:6}") private int topK;

    public RerankResult recommend(
            Long cityId, double centerLat, double centerLng,
            double radiusKm, UserContext user) throws Exception {

        // 1) 후보 페치 (인기/거리 기준 상위 파이프라인)
        List<RerankCandidate> candidates = fetchAndMapCandidates(cityId, centerLat, centerLng, radiusKm, maxCandidates);

        // 2) LLM 재랭킹 호출
        Map<String, Object> payload = Map.of(
                "messages", List.of(
                        Map.of("role", "system", "content", RERANK_SYSTEM),
                        Map.of("role", "user", "content", Map.of(
                                "user", Map.of(
                                        "time_mode", user.timeMode(),
                                        "travel_style", user.travelStyle(),
                                        "companions", user.companions(),
                                        "preferences", user.preferences(),
                                        "max_spots", user.maxSpots()
                                ),
                                "candidates", candidates,
                                "hints", Map.of("topK", topK)
                        ))
                )
        );

        String resp = clovaWebClient.post()
                .headers(clovaHeaders.apply())
                .bodyValue(om.writeValueAsString(payload))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        RerankResult result = parseRerankResponse(resp);

        // 3) 서버 가드레일(중복/동선/카테고리 편중 보정) — 필요시 후처리
        return guardRails(result, candidates);
    }

    private List<RerankCandidate> fetchAndMapCandidates(
            Long cityId, double lat, double lng, double radiusKm, int max
    ) {
        var rows = repo.findCandidates(cityId, lat, lng, radiusKm, max, 0);

        return rows.stream().map(r -> {
            double popularity = normPopularity(r.mainWeight(), r.checkCount());
            var meta = parseMeta(r.computedMeta());
            return new RerankCandidate(
                    r.id(), r.spotName(), r.category(),
                    r.distKm(), popularity, meta
            );
        }).toList();
    }

    private double normPopularity(Integer main, Integer checks) {
        // 간단 가중합(실서비스는 min-max/백분위 기반)
        double m = Optional.ofNullable(main).orElse(0);
        double c = Optional.ofNullable(checks).orElse(0);
        return Math.tanh( (0.7*m + 0.3*c) / 1000.0 ); // 0~1 근사
    }

    private Map<String, Object> parseMeta(String json) {
        if (json == null || json.isBlank()) return Map.of();
        try { return om.readValue(json, new TypeReference<>(){}); }
        catch (Exception e) { return Map.of(); }
    }

    private RerankResult parseRerankResponse(String resp) throws Exception {
        // 모델 응답에서 JSON 객체 추출
        int s = resp.indexOf('{'), e = resp.lastIndexOf('}');
        String json = (s >= 0 && e > s) ? resp.substring(s, e + 1) : "{}";
        return om.readValue(json, RerankResult.class);
    }

    private RerankResult guardRails(RerankResult r, List<RerankCandidate> pool) {
        // 중복 제거, 카테고리 다양성, 간단 거리 보정 등 최소 검증 로직(필요시 추가)
        return r;
    }

    private static final String RERANK_SYSTEM = """
        역할: 당신은 도시 내부 관광지 추천·동선 최적화 전문가다.
        목표: [user] 조건과 [candidates]를 바탕으로 상위 N개 관광지를 선정하고,
        야간 여부/스타일/동행과 거리·인기 힌트를 고려해 간단 방문 순서를 만든다.
        규칙:
        - 다양성: 동일 카테고리 과다 연속 금지
        - 거리: 되도록 가까운 곳끼리 묶고 왕복 최소화
        - 야간 모드면 night_suitability 높은 후보 우선
        - JSON만 출력:
        {
          "top_spots":[{"id":number,"rank":number,"reason":string}],
          "route":[{"order":number,"id":number}],
          "notes":string
        }
        """;
}

