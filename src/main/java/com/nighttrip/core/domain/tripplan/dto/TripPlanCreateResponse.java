package com.nighttrip.core.domain.tripplan.dto;

import java.time.LocalDate;
import java.util.List;

public record TripPlanCreateResponse(
        Long tripPlanId,
        LocalDate startDate,
        LocalDate endDate,
        List<CityResponse> cities
) {
    public record CityResponse(Long id, String cityName) {}
}