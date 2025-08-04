package com.nighttrip.core.domain.tripday.dto;

public record TripPlanChangeOrderRequest(
        Integer from_index,
        Integer to_index
) {
}
