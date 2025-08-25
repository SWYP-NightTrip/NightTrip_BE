package com.nighttrip.core.global.ai.dto;

import java.util.List;

public record RerankResult(
        List<RankedSpot> topSpots,   // id, rank, reason
        List<RouteStep> route,       // order, id
        String notes
) {
    public record RankedSpot(Long id, int rank, String reason) {}
    public record RouteStep(int order, Long id) {}
}