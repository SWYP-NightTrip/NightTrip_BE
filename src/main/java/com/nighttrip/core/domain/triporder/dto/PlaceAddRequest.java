package com.nighttrip.core.domain.triporder.dto;

public record PlaceAddRequest(
        String placeName,
        String placeAddress,
        String placeDetailAddress,
        String tripPlanName,
        Integer tripDayOrder
) {
}
