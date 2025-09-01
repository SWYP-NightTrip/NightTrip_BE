package com.nighttrip.core.ai.service;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nighttrip.core.ai.dto.LabelInputSpot;
import com.nighttrip.core.ai.dto.LabeledMeta;
import com.nighttrip.core.ai.header.ClovaHeaders;
import com.nighttrip.core.domain.touristspot.entity.TouristSpot;
import com.nighttrip.core.domain.touristspot.repository.TouristSpotRepository;
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

    private static List<String> toDetailStrings(Set<SpotDetails> set) {
        if (set == null || set.isEmpty()) return List.of();
        return set.stream()
                .map(Enum::name)
                .sorted()
                .collect(Collectors.toList());
    }

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

    private int callAndUpsert(List<LabelInputSpot> batch, List<TouristSpot> pending) throws Exception {
        final String LABEL_SYSTEM_STRICT = """
                    역할: 당신은 도시 내 관광지 정보를 요약·태깅하는 전문가다.
                
                                규칙(반드시 준수):
                                - "출력은 JSON 배열 하나"만 허용. 설명/코멘트/코드펜스/기타 텍스트 금지.
                                - 필수 키/옵션 키 스키마를 정확히 지켜라. 누락/추가 키 금지(옵션 키는 존재할 때만 포함).
                                - 값 제약:
                                  [필수]
                                    - id: number
                                    - tags: string[] (최대 5)
                                    - night_suitability: number (0~1)
                                    - dwell_time_min: "숫자~숫자" (분 단위)
                                    - pros: string[] (<=3), cons: string[] (<=2), must_know: string[] (<=2), evidence: string[] (<=3)
                                  [옵션]
                                    - category: "숙소"|"식당"|"카페"|"펍"|"캠핑"|"랜드마크"|"전망대"|"공원"|"해변"|"드라이브코스"|"행사"
                                    - lat: number, lng: number
                                    - price_level: 1|2|3|4|5
                                    - open_hours: [{"dow":"Mon|Tue|...","from":"HH:mm","to":"HH:mm"}, ...]
                                    - night_open: boolean
                                    - is_event: boolean
                                    - event_dates: string[]  // "YYYY-MM-DD"
                                    - is_night_view_spot: boolean
                                    - night_view_score: number (0~1)
                                    - is_rooftop: boolean
                                    - live_music: boolean
                                    - drive_walk_scenic: number (0~1)
                                    - capacity_hint: number  // 권장/수용 인원 추정치
                
                                원칙:
                                - 입력 정보 부족 시 보수적으로 추론하되 거짓 단정 금지. 애매하면 중립값.
                                - JSON 문법 준수. null/NaN/undefined 금지.
                                - 다시 강조: JSON 이외 그 어떤 텍스트도 출력 금지.
                """;

        // 2) 사용자 프롬프트: 스키마 설명 + spots 데이터
        var userPrompt = Map.of(
                "role", "user",
                "content", """
                        입력: 관광지의 이름, 카테고리, 소개문, 상세설명, 주소, 좌표.
                        출력: 아래 스키마의 JSON 배열만 응답하라.
                        
                        각 요소 스키마(필수/옵션 구분):
                        {
                          "id": number,
                          "tags": string[<=5],
                          "night_suitability": number,    // 0~1
                          "dwell_time_min": "30~90",
                          "pros": string[<=3],
                          "cons": string[<=2],
                          "must_know": string[<=2],
                          "evidence": string[<=3],
                        
                          // 옵션
                          "category": "...",
                          "lat": number, "lng": number,
                          "price_level": 1|2|3|4|5,
                          "open_hours": [{"dow":"Mon","from":"10:00","to":"23:00"}],
                          "night_open": true|false,
                          "is_event": true|false,
                          "event_dates": ["2025-10-03","2025-10-04"],
                          "is_night_view_spot": true|false,
                          "night_view_score": number,
                          "is_rooftop": true|false,
                          "live_music": true|false,
                          "drive_walk_scenic": number,
                          "capacity_hint": number
                        }
                        
                        JSON 외 텍스트 금지.
                        
                        %s
                        """.formatted(
                        om.writeValueAsString(
                                Map.of(
                                        "spots", batch,
                                        // 참고용 힌트(라벨 값 자체는 입력 기반으로 보수적 추정):
                                        "context_hints", Map.of(
                                                "tripDuration", /* ctx.tripDuration() 등 필요시 전달 */
                                                "travelTime",   /* ctx.travelTime() */
                                                "purpose",      /* ctx.purpose() */
                                                "budgetLevel",  /* ctx.budgetLevel() */
                                                "groupSize",    /* ctx.groupSize() */
                                                "extras"     /* ctx.extras() */
                                        )
                                )
                        )
                )
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
