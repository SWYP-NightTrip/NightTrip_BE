package com.nighttrip.core.global.ai.dto;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Comparator;
import java.util.List;

public record RerankResult(
        List<RankedSpot> topSpots,   // id, rank, reason
        List<RouteStep> route,       // order, id
        String notes
) {
    public record RankedSpot(Long id, int rank, String reason) {}
    public record RouteStep(int order, Long id) {}

    private static final class ArrayItem {
        public Long id;
        public Integer rank;
        public String reason;
    }

    /** Clova v3 chat-completions의 content에 담긴 JSON 배열을 RerankResult로 변환 */
    public static RerankResult fromArrayJson(String arrJson, ObjectMapper om) throws Exception {
        JavaType listType = om.getTypeFactory().constructCollectionType(List.class, ArrayItem.class);
        List<ArrayItem> items = om.readValue(arrJson, listType);
        if (items == null || items.isEmpty()) {
            return new RerankResult(List.of(), List.of(), null);
        }

        // rank 오름차순 정렬 + 유효성 필터
        items = items.stream()
                .filter(i -> i != null && i.id != null && i.rank != null)
                .sorted(Comparator.comparingInt(i -> i.rank))
                .toList();

        List<RankedSpot> top = items.stream()
                .map(i -> new RankedSpot(i.id, i.rank, i.reason == null ? "" : i.reason))
                .toList();

        List<RouteStep> rt = items.stream()
                .map(i -> new RouteStep(i.rank, i.id))  // order = rank
                .toList();

        return new RerankResult(top, rt, null);
    }
}