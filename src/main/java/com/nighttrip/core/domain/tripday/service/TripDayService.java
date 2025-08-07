package com.nighttrip.core.domain.tripday.service;

import com.nighttrip.core.domain.tripday.dto.TripPlanChangeOrderRequest;

public interface TripDayService {
    void changePlanOrder(TripPlanChangeOrderRequest request, Long tripPlanId, Integer tripDayId);
}
