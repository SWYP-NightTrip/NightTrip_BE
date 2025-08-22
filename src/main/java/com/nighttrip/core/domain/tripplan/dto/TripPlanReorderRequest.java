package com.nighttrip.core.domain.tripplan.dto;

public record TripPlanReorderRequest(
        Long tripPlanId,
        Long fromIndex,
        Long toIndex
) {
}
