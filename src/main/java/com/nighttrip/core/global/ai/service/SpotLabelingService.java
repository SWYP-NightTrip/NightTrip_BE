package com.nighttrip.core.global.ai.service;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nighttrip.core.domain.touristspot.entity.TouristSpot;
import com.nighttrip.core.domain.touristspot.repository.TouristSpotRepository;
import com.nighttrip.core.global.ai.ClovaHeaders;
import com.nighttrip.core.global.ai.dto.LabelInputSpot;
import com.nighttrip.core.global.ai.dto.LabeledMeta;
import com.nighttrip.core.global.enums.SpotDetails;
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class SpotLabelingService {

    private static final String LABEL_SYSTEM = """
            역할: 당신은 도시 내 관광지 정보를 요약·태깅하는 전문가다.
            입력: 관광지의 이름, 카테고리, 소개문, 상세설명, 주소, 좌표.
            출력: JSON 배열로만 응답하라. 각 요소 스키마:
            {
              "id": number,
              "tags": string[<=5],
              "night_suitability": number(0~1),
              "style": string[] (예: ["감성","느긋한","활동적","가성비","럭셔리"]),
              "companions_fit": string[] (["가족","커플","친구","혼자"]),
              "dwell_time_min": "30~90",
              "pros": string[<=3], "cons": string[<=2], "must_know": string[<=2],
              "evidence": string[<=3]
            }
            JSON 외 텍스트 금지.
            """;
    private final TouristSpotRepository repo;
    private final WebClient clovaWebClient;
    private final ClovaHeaders clovaHeaders;
    private final ObjectMapper om;
    @Value("${llm.label.batchSize:40}")
    private int batchSize;
    @Value("${llm.label.metaVersion:1}")
    private int metaVersion;

    // 라벨링할 대상 페이지 단위로 긁어와서 배치 호출
    @Transactional
    public int labelCity(Long cityId) throws Exception {
        int offset = 0, totalUpdated = 0;
        final int pageSize = 500; // DB 페치(4만건 대비)
        while (true) {
            List<TouristSpot> page = fetchPage(cityId, offset, pageSize);
            if (page.isEmpty()) break;

            // LLM에 보낼 batch 구성
            List<LabelInputSpot> batch = new ArrayList<>();
            List<TouristSpot> pending = new ArrayList<>();
            for (TouristSpot ts : page) {
                // 이미 최신 라벨이면 스킵
                if (ts.getComputedMeta() != null && Objects.equals(ts.getMetaVersion(), metaVersion)) continue;
                batch.add(toLabelInput(ts));
                pending.add(ts);
                if (batch.size() == batchSize) {
                    totalUpdated += callAndUpsert(batch, pending);
                    batch.clear();
                    pending.clear();
                }
            }
            // 남은 것 처리
            if (!batch.isEmpty()) {
                totalUpdated += callAndUpsert(batch, pending);
            }
            offset += pageSize;
        }
        return totalUpdated;
    }

    private List<TouristSpot> fetchPage(Long cityId, int offset, int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit);
        return repo.findByCityId(cityId, pageable).getContent();
    }

    private LabelInputSpot toLabelInput(TouristSpot ts) {
        String categoryStr = (ts.getCategory() == null) ? null : ts.getCategory().name();

        List<String> details =
                toDetailStrings(ts.getTouristSpotDetails()); // null → null 또는 [] 중 택1

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
        if (set == null || set.isEmpty()) return List.of(); // LLM엔 []가 편합니다
        return set.stream()
                .map(Enum::name)               // 한글 라벨 원하면 .map(SpotDetails::getKoLabel)
                .sorted()
                .collect(Collectors.toList());
    }

    private int callAndUpsert(List<LabelInputSpot> batch, List<TouristSpot> pending) throws Exception {
        Map<String, Object> payload = Map.of(
                "messages", List.of(
                        Map.of("role", "system", "content", LABEL_SYSTEM),
                        Map.of("role", "user", "content", Map.of(
                                "spots", batch,
                                "time_mode", "night"
                        ))
                )
        );
        String body = om.writeValueAsString(payload);

        String resp = clovaWebClient.post()
                .headers(clovaHeaders.apply())
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // 모델 응답에서 JSON 배열 추출(모델별 응답 래핑 형식에 맞춰 파싱 필요)
        List<LabeledMeta> metas = parseLabelResponse(resp);

        Map<Long, String> metaJsonById = metas.stream()
                .collect(Collectors.toMap(LabeledMeta::id, m -> safeWriteValue(m)));

        int updated = 0;
        for (TouristSpot ts : pending) {
            String metaJson = metaJsonById.get(ts.getId());
            if (metaJson == null) continue; // 실패시 다음 배치에서 재시도
            ts.changeComputedMeta(metaJson);
            ts.changeMetaVersion(metaVersion);
            ts.changeMetaUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
            updated++;
        }
        // JPA flush는 트랜잭션 종료 시점에 반영
        return updated;
    }

    private List<LabeledMeta> parseLabelResponse(String resp) throws Exception {
        // Clova 응답 형태에 맞춰 messages[...].content 또는 result 텍스트에서 JSON 추출
        // 여기서는 바로 List<LabeledMeta> 로 들어온다고 가정
        JavaType type = om.getTypeFactory().constructCollectionType(List.class, LabeledMeta.class);
        return om.readValue(extractJsonArray(resp), type);
    }

    private String extractJsonArray(String resp) {
        // 모델이 텍스트에 JSON만 넣도록 프롬프트했지만, 방어적으로 [] 부분만 추출
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
