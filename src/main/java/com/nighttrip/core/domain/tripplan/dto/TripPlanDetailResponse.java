package com.nighttrip.core.domain.tripplan.dto;


import java.util.List;

public record TripPlanDetailResponse(
        Long tripPlanId,
        String title,
        List<TripDayDetailResponse> tripDays
) {
}