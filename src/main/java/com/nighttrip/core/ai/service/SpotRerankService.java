package com.nighttrip.core.ai.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nighttrip.core.ai.dto.RerankCandidate;
import com.nighttrip.core.ai.dto.SpotRowDto;
import com.nighttrip.core.ai.dto.UserContext;
import com.nighttrip.core.ai.header.ClovaHeaders;
import com.nighttrip.core.ai.repository.TouristSpotRepositoryAi;
import com.nighttrip.core.domain.touristspot.dto.RecommendTouristSpotResponse;
import com.nighttrip.core.global.enums.ImageSizeType;
import com.nighttrip.core.global.enums.ImageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpotRerankService {

    private static final String BUCKET_RERANK_SYS = """
            역할: 당신은 도시 내 관광지 추천 재랭킹 전문가다.
            입력: {"user":{...}, "section":"...", "candidates":[...], "hints":{"topK":K}}
            목표:
            - 입력 candidates와 사용자 컨텍스트를 기반으로 Top-K를 선정하고 각 항목에 한국어 1~2문장 reason을 쓴다.
            
            선정 규칙(공통, 필수):
            1) travelTime이 "저녁/심야"이면 meta.night_suitability 가점.
            2) purpose/extras ↔ meta 태그/플래그 일치 가점.
            3) budgetLevel ↔ meta.price_level 매칭(한 단계 차이는 약한 패널티).
            4) groupSize ↔ meta.capacity_hint 부합 시 가점.
            5) open_hours와 travelTime 불일치 시 강한 패널티 또는 제외(심야는 22:00 이후 영업 또는 night_open).
            6) popularity 높을수록 가점.
            7) 날짜 미지정: is_event/night_open 가점, event_dates는 참고만.
            
            출력 형식(엄격):
            [
              { "id": number, "reason": "한국어 1~2문장", "score": number, "rank": number },
              ...
            ]
            
            제약:
            - 반드시 JSON 배열만 출력. 코드펜스/설명/주석 금지.
            - 길이는 정확히 K(hints.topK).
            - id는 입력 candidates에서만 선택, 중복 금지.
            - rank는 1..K 오름차순, score는 0..1 범위.
            - 모든 키에 큰따옴표 사용, null/NaN/undefined/trailing comma 금지.
            """;

    private final TouristSpotRepositoryAi repo;
    private final WebClient clovaWebClient;
    private final ClovaHeaders clovaHeaders;
    private final ObjectMapper om;

    @Value("${llm.rerank.topK:10}")
    private int topK;

    // 느슨한 ObjectMapper (지연 초기화)
    private ObjectMapper laxOm;

    // ---------------- Seed 생성 ----------------
    private List<RerankCandidate> seed(UserContext u, List<RerankCandidate> pool, int seedK) {
        if (pool == null || pool.isEmpty()) return List.of();
        int cap = Math.min(Math.max(1, seedK), 24); // 1..24
        return pool.stream()
                .sorted((c1, c2) -> Double.compare(score(u, c2), score(u, c1)))
                .limit(cap)
                .toList();
    }

    // ---------------- LLM Rerank ----------------
    private BucketRerankResult llmRerank(UserContext u, List<RerankCandidate> seeds, int topK) {
        if (topK <= 0) return new BucketRerankResult(List.of(), Map.of());
        if (seeds == null || seeds.isEmpty()) return new BucketRerankResult(List.of(), Map.of());

        var payload = Map.of(
                "user", Map.of(
                        "tripDuration", u.tripDuration(),
                        "travelTime", u.travelTime(),
                        "purpose", u.purpose(),
                        "budgetLevel", u.budgetLevel(),
                        "groupSize", u.groupSize(),
                        "extras", u.extras()
                ),
                // 단일 섹션: 고정 문자열만 사용(버킷 개념 제거)
                "section", "main",
                "candidates", seeds.stream().map(c -> Map.of(
                        "id", c.id(),
                        "spotName", c.spotName(),
                        "category", c.category(),
                        "popularity", c.popularity(),
                        "meta", Optional.ofNullable(c.meta()).orElse(Map.of())
                )).toList(),
                "hints", Map.of("topK", topK)
        );

        Map<String, Object> body;
        try {
            body = Map.of(
                    "messages", List.of(
                            Map.of("role", "system", "content", BUCKET_RERANK_SYS),
                            Map.of("role", "user", "content", om.writeValueAsString(payload))
                    ),
                    "topP", 0.8, "topK", 0, "maxTokens", 1536, "temperature", 0.2,
                    "repetitionPenalty", 1.1, "includeAiFilters", true
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

        Map<Long, RerankCandidate> seedMap = new HashMap<>();
        for (var c : seeds) seedMap.put(c.id(), c);

        try {
            String raw = extractJsonArray(resp);
            List<Map<String, Object>> arr = parseReasonArray(raw);

            // rank 오름차순 정렬
            arr.sort(Comparator.comparingInt(m -> {
                Object rv = m.get("rank");
                if (rv instanceof Number n) return n.intValue();
                try { return Integer.parseInt(String.valueOf(rv)); }
                catch (Exception e) { return Integer.MAX_VALUE; }
            }));

            var picked = new ArrayList<RerankCandidate>();
            var reasons = new LinkedHashMap<Long, String>();

            for (var m : arr) {
                if (picked.size() >= topK) break;
                Object idv = m.get("id");
                if (idv == null) continue;

                Long id;
                try {
                    id = (idv instanceof Number n) ? n.longValue() : Long.parseLong(String.valueOf(idv));
                } catch (Exception ignore) {
                    continue;
                }

                var cand = seedMap.get(id);
                if (cand == null) continue; // seed 밖 id 방지

                String rs = (m.get("reason") == null) ? null : safeReason(String.valueOf(m.get("reason")));
                picked.add(cand);
                reasons.put(id, (rs == null || rs.isBlank()) ? makeFallbackReason(cand) : rs);
            }

            // 부족 시: seed 상위 점수로 채우기
            if (picked.size() < topK) {
                var fill = seeds.stream()
                        .filter(c -> picked.stream().noneMatch(p -> p.id().equals(c.id())))
                        .sorted((c1, c2) -> Double.compare(score(u, c2), score(u, c1)))
                        .limit(topK - picked.size())
                        .toList();
                for (var c : fill) {
                    picked.add(c);
                    reasons.putIfAbsent(c.id(), makeFallbackReason(c));
                }
            }

            return new BucketRerankResult(picked, reasons);
        } catch (Exception e) {
            log.warn("[LLM] parsing failed, fallback to heuristics", e);
            var picked = rankCandidates(u, seeds, topK);
            var reasons = new LinkedHashMap<Long, String>();
            for (var c : picked) reasons.put(c.id(), makeFallbackReason(c));
            return new BucketRerankResult(picked, reasons);
        }
    }

    private List<RecommendTouristSpotResponse> toRankedDtosKeepOrderWithReasons(
            Map<Long, String> reasons, List<RerankCandidate> items) {

        if (items == null || items.isEmpty()) return List.of();

        var idsInOrder = items.stream().map(RerankCandidate::id).toList();
        var rows = repo.findRowsWithThumbByIds(
                idsInOrder, ImageType.TOURIST_SPOT, ImageSizeType.THUMBNAIL);

        Map<Long, SpotRowDto> rowById = new HashMap<>(rows.size());
        for (SpotRowDto r : rows) rowById.putIfAbsent(r.id(), r);

        List<RecommendTouristSpotResponse> out = new ArrayList<>(items.size());
        int rank = 1;
        for (RerankCandidate c : items) {
            var r = rowById.get(c.id());
            if (r == null) continue;

            String categoryKo = (r.category() != null ? r.category().getKoreanName() : null);

            out.add(new RecommendTouristSpotResponse(
                    r.id(), rank++,
                    reasons.getOrDefault(r.id(), null),
                    r.spotName(), r.address(),
                    categoryKo,
                    r.avgScope(),
                    r.reviewCount(),              // description 없음 → null
                    r.thumbUrl()
            ));
        }
        return out;
    }

    // ---------------- 단일 섹션 아이템 ----------------
    // (외부 시그니처 유지)
    public List<RecommendTouristSpotResponse> recommendSectionItems(UserContext ctx) {
        if (ctx == null || ctx.cityId() == null) throw new IllegalArgumentException("cityId가 필요합니다.");

        int limit = topK;
        int poolSize = Math.max(60, limit * 6);

        var pool = fetchAndMapCandidates(ctx.cityId(), poolSize);

        int seedK = Math.min(Math.max(limit * 3, 18), 24);
        var seeds = seed(ctx, pool, seedK);
        var rerank = llmRerank(ctx, seeds, limit);

        List<RerankCandidate> items = rerank.items;
        Map<Long, String> reasons = rerank.reasons;

        if (items == null || items.isEmpty()) {
            items = (seeds.isEmpty() ? pool : seeds).stream()
                    .sorted((c1, c2) -> Double.compare(score(ctx, c2), score(ctx, c1)))
                    .limit(limit)
                    .toList();
            reasons = new LinkedHashMap<>();
            for (var c : items) reasons.put(c.id(), makeFallbackReason(c));
        }
        return toRankedDtosKeepOrderWithReasons(reasons, items);
    }

    // ---------------- 데이터 로딩/변환 ----------------
    private List<RerankCandidate> fetchAndMapCandidates(String  cityId, int max) {
        var rows = repo.findCandidates(cityId, max, 0);
        return rows.stream().map(r -> {
            double popularity = normPopularity(r.mainWeight(), r.checkCount());
            var meta = parseMeta(r.computedMeta());
            String categoryStr = (r.category() == null) ? null : r.category().name();
            return new RerankCandidate(r.id(), r.spotName(), categoryStr, popularity, meta);
        }).toList();
    }

    private List<RerankCandidate> rankCandidates(UserContext u, List<RerankCandidate> pool, int topK) {
        return pool.stream()
                .map(c -> Map.entry(c, score(u, c)))
                .sorted(Map.Entry.<RerankCandidate, Double>comparingByValue().reversed()
                        .thenComparing(e -> e.getKey().id()))
                .limit(topK)
                .map(Map.Entry::getKey)
                .toList();
    }

    // ---------------- 점수/규칙 ----------------
    private double score(UserContext u, RerankCandidate c) {
        Map<String, Object> m = Optional.ofNullable(c.meta()).orElseGet(HashMap::new);
        double w = 0.0;

        String tt = normTravelTime(u.travelTime());
        String pp = normPurpose(u.purpose());
        String bb = normBudgetLevel(u.budgetLevel());
        String ex = normExtras(u.extras());

        w += 0.60 * clamp01(c.popularity());
        if (isNight(tt)) w += 0.15 * dbl(m, "night_suitability", 0.0);

        w += 0.10 * purposeMatch(pp, m);
        w += 0.15 * extrasMatch(ex, m);

        w += 0.10 * timeOpenMatch(tt, m);
        w += 0.05 * capacityMatch(u.groupSize(), m);
        w -= budgetPenalty(bb, m);
        w -= hoursPenalty(tt, m);

        return clamp01(w);
    }

    // ---------------- 공통 헬퍼 ----------------
    private String makeFallbackReason(RerankCandidate c) {
        return c.spotName() + "은(는) 이번 여정에 잘 맞는 추천지예요.";
    }

    private double normPopularity(Integer main, Integer checks) {
        double m = Optional.ofNullable(main).orElse(0);
        double c = Optional.ofNullable(checks).orElse(0);
        return Math.tanh((0.7 * m + 0.3 * c) / 1000.0);
    }

    private String normGroupLabel(String s) {
        if (s == null) return null;
        return s.trim().replaceAll("\\s+", ""); // "온 가족 여행" -> "온가족여행"
    }

    private String norm(String s) {
        if (s == null) return null;
        return s.trim().replaceAll("\\s+", "");
    }

    // 여행시간 라벨 → "오전/오후/저녁/심야"
    private String normTravelTime(String raw) {
        if (raw == null) return null;
        String s = norm(raw);

        // 키워드 우선
        if (s.contains("심야")) return "심야";
        if (s.contains("저녁")) return "저녁";
        if (s.contains("오후")) return "오후";
        if (s.contains("오전")) return "오전";

        // 시간 범위 힌트
        if (s.contains("10시이후") || s.contains("22시") || s.contains("23시")) return "심야";
        if (s.contains("7-10시") || s.contains("19시") || s.contains("20시") || s.contains("21시")) return "저녁";
        if (s.contains("4-7시") || s.contains("16시") || s.contains("17시") || s.contains("18시")) return "오후";

        return null; // 매칭 실패 시 null
    }

    // 예산 라벨 → "저렴/보통/프리미엄"
// 기준(임시): ≤20만 = 저렴, ≤40만 = 보통, >40만 = 프리미엄
    private String normBudgetLevel(String raw) {
        if (raw == null) return null;
        String s = norm(raw);

        // 숫자(만 원 단위) 추출
        var m = java.util.regex.Pattern.compile("(\\d+)만").matcher(s);
        java.util.List<Integer> vals = new java.util.ArrayList<>();
        while (m.find()) vals.add(Integer.parseInt(m.group(1)));
        int lo = vals.isEmpty() ? 0 : vals.get(0);
        int hi = vals.size() >= 2 ? vals.get(1) : (s.contains("이상") ? Integer.MAX_VALUE : lo);

        int ref = (hi == Integer.MAX_VALUE) ? hi : (lo + hi) / 2; // 중앙값 또는 하한
        if (ref <= 20) return "저렴";
        if (ref <= 40) return "보통";
        return "프리미엄";
    }

    // 스타일 라벨 → 목적("힐링/사진/미식/액티브")
    private String normPurpose(String raw) {
        if (raw == null) return null;
        String s = norm(raw);
        if (s.contains("힐링") || s.contains("자연")) return "힐링";
        if (s.contains("맛집") || s.contains("맛폿") || s.contains("미식") || s.contains("투어")) return "미식";
        if (s.contains("사진") || s.contains("별") || s.contains("야경") || s.contains("포토")) return "사진";
        if (s.contains("드라이브") || s.contains("산책") || s.contains("축제") || s.contains("체험")) return "액티브";
        return raw; // 매칭 안 되면 원문 유지
    }

    // 스타일 라벨/자유입력 → extras 표준키("루프탑/라이브/야시장/야간개장")
    private String normExtras(String raw) {
        if (raw == null) return null;
        String s = norm(raw);
        if (s.contains("루프탑")) return "루프탑";
        if (s.contains("라이브") || s.contains("펍") || s.contains("라이브카페")) return "라이브";
        if (s.contains("야시장")) return "야시장";
        if (s.contains("야간개장") || s.contains("심야문화") || s.contains("야간축제") || s.contains("야간")) return "야간개장";
        return raw; // 매칭 안 되면 원문
    }

    private int[] groupRange(String normalizedLabel) {
        if (normalizedLabel == null) return null;
        return switch (normalizedLabel) {
            case "혼자여행"     -> new int[]{1, 1};
            case "친구와함께"   -> new int[]{2, 5};
            case "연인과함께"   -> new int[]{2, 2};
            case "배우자와함께" -> new int[]{2, 2};
            case "부모님과함께" -> new int[]{3, 4};
            case "온가족여행"   -> new int[]{3, 6};
            case "그외"         -> null;  // 미지정
            default             -> null;
        };
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

    private String extractJsonArray(String resp) {
        try {
            JsonNode root = om.readTree(resp);
            String content = root.path("result").path("message").path("content").asText("");
            int s = content.indexOf('[');
            int e = content.lastIndexOf(']');
            if (s >= 0 && e > s) return content.substring(s, e + 1);
            s = resp.indexOf('[');
            e = resp.lastIndexOf(']');
            return (s >= 0 && e > s) ? resp.substring(s, e + 1) : "[]";
        } catch (Exception e) {
            log.error("[RERANK] v3 content 추출 실패", e);
            return "[]";
        }
    }

    private double dbl(Map<String, Object> m, String k, double def) {
        Object v = m.get(k);
        if (v instanceof Number n) return n.doubleValue();
        try { return v == null ? def : Double.parseDouble(String.valueOf(v)); }
        catch (Exception ignore) { return def; }
    }

    private boolean bool(Map<String, Object> m, String k) {
        Object v = m.get(k);
        if (v instanceof Boolean b) return b;
        return "true".equalsIgnoreCase(String.valueOf(v));
    }

    private boolean hasTag(Map<String, Object> m, String tag) {
        var tags = (List<?>) m.getOrDefault("tags", List.of());
        return tags.stream().anyMatch(t -> tag.equals(String.valueOf(t)));
    }

    @SuppressWarnings("unchecked")
    private boolean openAt(Map<String, Object> m, String hhmm) {
        var hours = (List<Map<String, Object>>) m.getOrDefault("open_hours", List.of());
        for (var h : hours) {
            String from = String.valueOf(h.getOrDefault("from", "00:00"));
            String to = String.valueOf(h.getOrDefault("to", "23:59"));
            if (from.compareTo(hhmm) <= 0 && to.compareTo(hhmm) >= 0) return true;
        }
        return false;
    }

    private double purposeMatch(String purpose, Map<String, Object> m) {
        if (purpose == null) return 0;
        return switch (purpose) {
            case "힐링" -> scoreTags(m, List.of("힐링", "산책", "조용함", "쉼", "스파", "온천"));
            case "사진" -> scoreTags(m, List.of("야경", "전망", "포토스팟", "루프탑"));
            case "미식" -> scoreTags(m, List.of("맛집", "야시장", "펍", "라이브"));
            case "액티브" -> scoreTags(m, List.of("액티비티", "드라이브", "축제", "야시장"));
            default -> 0;
        };
    }

    private double extrasMatch(String extras, Map<String, Object> m) {
        if (extras == null) return 0.0;
        String x = extras.trim();
        if (x.isEmpty()) return 0.0;

        return switch (x) {
            case "루프탑"   -> bool(m, "is_rooftop") ? 1.0 : 0.0;
            case "라이브"   -> bool(m, "live_music") ? 1.0 : 0.0;
            case "야시장"   -> hasTag(m, "야시장") ? 1.0 : 0.0;
            case "야간개장" -> (hasTag(m, "야간개장") || bool(m, "night_open")) ? 1.0 : 0.0;
            default        -> hasTag(m, x) ? 1.0 : 0.0;
        };
    }

    private double timeOpenMatch(String travelTime, Map<String, Object> m) {
        if (travelTime == null) return 0;
        return switch (travelTime) {
            case "오전" -> openAt(m, "10:00") ? 1.0 : 0.0;
            case "오후" -> openAt(m, "15:00") ? 1.0 : 0.0;
            case "저녁" -> openAt(m, "19:00") || bool(m, "night_open") ? 1.0 : 0.0;
            case "심야" -> openAt(m, "22:00") || bool(m, "night_open") ? 1.0 : 0.0;
            default -> 0.0;
        };
    }

    private boolean isNight(String travelTime) {
        return "저녁".equals(travelTime) || "심야".equals(travelTime);
    }

    private double capacityMatch(String groupSizeLabel, Map<String, Object> m) {
        String norm = normGroupLabel(groupSizeLabel);
        int[] rng = groupRange(norm);
        if (rng == null) return 0.0; // 라벨이 없거나 매핑 불가

        Object v = m.get("capacity_hint");
        Integer cap = (v instanceof Number n) ? n.intValue() : null;
        if (cap == null) return 0.0; // 메타에 권장 인원 없으면 가점 없음

        // 범위 내면 만점
        if (cap >= rng[0] && cap <= rng[1]) return 1.0;

        // 범위에서 벗어난 정도에 따라 완화
        int dist = (cap < rng[0]) ? (rng[0] - cap) : (cap - rng[1]);
        if (dist == 1) return 0.7;
        if (dist == 2) return 0.5;
        return 0.0;
    }


    private double budgetPenalty(String budgetLevel, Map<String, Object> m) {
        if (budgetLevel == null) return 0;
        Integer pl = null;
        Object v = m.get("price_level");
        if (v instanceof Number n) pl = n.intValue(); // 1~5
        if (pl == null) return 0;

        int wantMin, wantMax;
        switch (budgetLevel) {
            case "저렴" -> { wantMin = 1; wantMax = 2; }
            case "보통" -> { wantMin = 2; wantMax = 3; }
            case "프리미엄" -> { wantMin = 4; wantMax = 5; }
            default -> { return 0; }
        }
        if (pl >= wantMin && pl <= wantMax) return 0;
        if (Math.abs(pl - ((wantMin + wantMax) / 2.0)) <= 1.0) return 0.2;
        return 0.5;
    }

    private double scoreTags(Map<String, Object> m, List<String> wants) {
        var tags = (List<?>) m.getOrDefault("tags", List.of());
        if (tags.isEmpty()) return 0;
        int hit = 0;
        for (Object t : tags) if (wants.contains(String.valueOf(t))) hit++;
        return (double) hit / wants.size();
    }

    private double hoursPenalty(String travelTime, Map<String, Object> m) {
        if (travelTime == null) return 0;
        boolean open = switch (travelTime) {
            case "오전" -> openAt(m, "10:00");
            case "오후" -> openAt(m, "15:00");
            case "저녁" -> openAt(m, "19:00") || bool(m, "night_open");
            case "심야" -> openAt(m, "22:00") || bool(m, "night_open");
            default -> false;
        };
        return open ? 0.0 : 0.6;
    }

    private double clamp01(double x) { return x < 0 ? 0 : (x > 1 ? 1 : x); }

    private ObjectMapper getLaxOm() {
        if (laxOm == null) {
            laxOm = om.copy()
                    .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
                    .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
                    .configure(JsonReadFeature.ALLOW_TRAILING_COMMA.mappedFeature(), true);
        }
        return laxOm;
    }

    private String cleanModelText(String raw) {
        if (raw == null) return "[]";
        String s = raw.replace("```json", "").replace("```", "").trim();
        int sIdx = s.indexOf('['), eIdx = s.lastIndexOf(']');
        if (sIdx >= 0 && eIdx > sIdx) s = s.substring(sIdx, eIdx + 1);
        s = s.replaceAll(",\\s*(\\]|\\})", "$1"); // 트레일링 콤마 제거
        s = s.replaceAll("(?i)(\\{|,)(\\s*)(id|reason|score|rank)(\\s*):", "$1$2\"$3\"$4:");
        return s;
    }

    private List<Map<String, Object>> parseReasonArray(String raw) {
        String s = cleanModelText(raw);
        try {
            return om.readValue(s, new TypeReference<>() {});
        } catch (Exception e1) {
            try {
                return getLaxOm().readValue(s, new TypeReference<>() {});
            } catch (Exception e2) {
                // 매우 깨진 경우: id & reason만 정규식으로 구제
                List<Map<String, Object>> out = new ArrayList<>();
                var p = java.util.regex.Pattern.compile(
                        "id\\s*[:=]\\s*(\\d+)[^\\]]*?reason\\s*[:=]\\s*(?:\"([^\"]*)\"|'([^']*)')",
                        java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.DOTALL
                );
                var m = p.matcher(raw);
                int rank = 1;
                while (m.find()) {
                    long id = Long.parseLong(m.group(1));
                    String rs = m.group(2) != null ? m.group(2) : m.group(3);
                    out.add(Map.of("id", id, "reason", safeReason(rs), "rank", rank++, "score", 1.0));
                }
                return out;
            }
        }
    }

    private String safeReason(String s) {
        if (s == null) return null;
        String t = s.replaceAll("\\s+", " ").trim();
        return t.length() > 140 ? t.substring(0, 140) + "…" : t;
    }

    // 간단한 결과 홀더
    private static final class BucketRerankResult {
        final List<RerankCandidate> items;
        final Map<Long, String> reasons;

        BucketRerankResult(List<RerankCandidate> items, Map<Long, String> reasons) {
            this.items = items;
            this.reasons = reasons;
        }
    }
}
