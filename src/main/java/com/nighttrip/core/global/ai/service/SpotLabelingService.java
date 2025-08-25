package com.nighttrip.core.global.ai.service;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nighttrip.core.domain.touristspot.entity.TouristSpot;
import com.nighttrip.core.domain.touristspot.repository.TouristSpotRepository;
import com.nighttrip.core.global.ai.header.ClovaHeaders;
import com.nighttrip.core.global.ai.dto.LabelInputSpot;
import com.nighttrip.core.global.ai.dto.LabeledMeta;
import com.nighttrip.core.global.enums.SpotDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpotLabelingService {

    private final TouristSpotRepository repo;
    private final WebClient clovaWebClient;
    private final ClovaHeaders clovaHeaders;
    private final ObjectMapper om;

    @Value("${llm.label.batchSize:40}")
    private int batchSize;
    @Value("${llm.label.metaVersion:1}")
    private int metaVersion;

    @Transactional
    public int labelCity(Long cityId) throws Exception {
        int offset = 0, totalUpdated = 0;
        final int pageSize = 500;

        log.info("[LABEL] 도시 라벨링 시작 | cityId={} | batchSize={} | pageSize={}", cityId, batchSize, pageSize);

        while (true) {
            List<TouristSpot> page = fetchPage(cityId, offset, pageSize);
            if (page.isEmpty()) break;

            log.info("[LABEL] {} 페이지 조회 | offset={} | 데이터 수={}", cityId, offset, page.size());

            List<LabelInputSpot> batch = new ArrayList<>();
            List<TouristSpot> pending = new ArrayList<>();

            for (TouristSpot ts : page) {
                if (ts.getComputedMeta() != null && Objects.equals(ts.getMetaVersion(), metaVersion)) {
                    log.debug("[LABEL] Skip: id={} (이미 최신 메타 적용됨)", ts.getId());
                    continue;
                }

                batch.add(toLabelInput(ts));
                pending.add(ts);

                // 배치 단위 처리
                if (batch.size() == batchSize) {
                    totalUpdated += callAndUpsert(batch, pending);
                    batch.clear();
                    pending.clear();
                }
            }

            if (!batch.isEmpty()) {
                totalUpdated += callAndUpsert(batch, pending);
            }
            offset += pageSize;
        }

        log.info("[LABEL] 도시 라벨링 완료 | cityId={} | 총 업데이트={}", cityId, totalUpdated);
        return totalUpdated;
    }

    private List<TouristSpot> fetchPage(Long cityId, int offset, int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit);
        return repo.findByCityId(cityId, pageable).getContent();
    }

    private LabelInputSpot toLabelInput(TouristSpot ts) {
        String categoryStr = (ts.getCategory() == null) ? null : ts.getCategory().name();
        List<String> details = toDetailStrings(ts.getTouristSpotDetails());

        return new LabelInputSpot(
                ts.getId(),
                ts.getSpotName(),
                categoryStr,
                ts.getAddress(),
                ts.getLatitude(),
                ts.getLongitude(),
                ts.getSpotDescription(),
                details
        );
    }

    private static List<String> toDetailStrings(Set<SpotDetails> set) {
        if (set == null || set.isEmpty()) return List.of();
        return set.stream()
                .map(Enum::name)
                .sorted()
                .collect(Collectors.toList());
    }

    private int callAndUpsert(List<LabelInputSpot> batch, List<TouristSpot> pending) throws Exception {
        final String LABEL_SYSTEM_STRICT = """
        역할: 당신은 도시 내 관광지 정보를 요약·태깅하는 전문가다.
        규칙(반드시 준수):
        - "출력은 JSON 배열 하나"만 허용. 설명/코멘트/코드펜스/키 이름 없는 텍스트 금지.
        - 키 순서와 스키마를 정확히 지켜라. 누락/추가 키 금지.
        - 값 제약:
          - id: number
          - tags: string 배열, 최대 5개
          - night_suitability: 0~1 사이 소수 (예: 0, 0.2, 0.5, 0.8, 1)
          - style: ["감성","느긋한","활동적","가성비","럭셔리"] 중 0개 이상
          - companions_fit: ["가족","커플","친구","혼자"] 중 1개 이상
          - dwell_time_min: "숫자~숫자" 형식 문자열 (분 단위)
          - pros: 최대 3개, cons: 최대 2개, must_know: 최대 2개
          - evidence: 최대 3개
        - 입력 정보 부족 시 보수적으로 추론, 거짓 단정 금지. 애매하면 중립값.
        - JSON 문법 준수. null/NaN/undefined 금지.
        - 다시 강조: JSON 이외 그 어떤 텍스트도 출력 금지.
    """;

        // 2) 사용자 프롬프트: 스키마 설명 + spots 데이터
        var userPrompt = Map.of(
                "role", "user",
                "content", """
            입력: 관광지의 이름, 카테고리, 소개문, 상세설명, 주소, 좌표.
            출력: JSON 배열로만 응답하라.
            각 요소 스키마: {
              "id": number,
              "tags": string[<=5],
              "night_suitability": number(0~1),
              "style": string[], // ["감성","느긋한","활동적","가성비","럭셔리"] 중
              "companions_fit": string[], // ["가족","커플","친구","혼자"]
              "dwell_time_min": "30~90",
              "pros": string[<=3],
              "cons": string[<=2],
              "must_know": string[<=2],
              "evidence": string[<=3]
            }
            JSON 외 텍스트 금지.

            %s
            """.formatted(om.writeValueAsString(Map.of("spots", batch, "time_mode", "night")))
        );

        Map<String, Object> body = Map.of(
                "messages", List.of(
                        Map.of("role", "system", "content", LABEL_SYSTEM_STRICT),
                        userPrompt
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

        String resp;
        try {
            resp = clovaWebClient.post()
                    .uri("/v3/chat-completions/HCX-005")
                    .headers(clovaHeaders.apply())
                    .bodyValue(body)                 // 객체 그대로 (문자열로 이중 직렬화하지 않음)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            log.info("[LABEL] Clova 응답 원본: {}", resp);
        } catch (Exception e) {
            log.error("[LABEL] Clova API 호출 실패 | spots={}", batch.stream().map(LabelInputSpot::id).toList(), e);
            throw e;
        }

        // 기존 파서 재사용: 응답 내부에서 최초 '['~마지막 ']' 구간을 추출하여 LabeledMeta[]로 역직렬화
        List<LabeledMeta> metas = parseLabelResponse(resp);

        Map<Long, String> metaJsonById = metas.stream()
                .collect(Collectors.toMap(LabeledMeta::id, this::safeWriteValue));

        int updated = 0;
        for (TouristSpot ts : pending) {
            String metaJson = metaJsonById.get(ts.getId());
            if (metaJson == null) continue;
            ts.changeComputedMeta(metaJson);
            ts.changeMetaVersion(metaVersion);
            ts.changeMetaUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
            updated++;
        }
        return updated;
    }

    private List<LabeledMeta> parseLabelResponse(String resp) throws Exception {
        JavaType type = om.getTypeFactory().constructCollectionType(List.class, LabeledMeta.class);
        return om.readValue(extractJsonArray(resp), type);
    }

    private String extractJsonArray(String resp) {
        int s = resp.indexOf('['), e = resp.lastIndexOf(']');
        return (s >= 0 && e > s) ? resp.substring(s, e + 1) : "[]";
    }

    private String safeWriteValue(Object o) {
        try {
            return om.writeValueAsString(o);
        } catch (Exception e) {
            return "{}";
        }
    }
}
