package com.nighttrip.core.domain.tripplan.dto;

import java.util.List;

public record TripDayDetailResponse(
        Long tripDayId,
        Integer dayOrder,
        List<CityResponse> cities,
        List<TripOrderResponse> tripOrders
) {
}
