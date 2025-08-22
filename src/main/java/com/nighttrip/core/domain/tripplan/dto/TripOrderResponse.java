package com.nighttrip.core.domain.tripplan.dto;

import com.nighttrip.core.domain.touristspot.dto.TouristSpotDetailResponse;

public record TripOrderResponse(
        Long tripOrderId,
        Long orderIndex,
        String arrivalTime,
        TouristSpotDetailResponse touristSpot
) {
}