package com.nighttrip.core.domain.triporder.service;

import java.util.List;

public interface TripOrderService {

    void updateArrivalTime(Long tripOrderId, String arrivalTime);

    void deleteTripOrder(Long tripOrderId);
    void addPlace(Long tripPlanId, Integer tripDayId, List<Long> touristSpotIds);
    void moveTripOrder(Long movingTripOrderId, Long originalTripDayId, Long destinationTripDayId, int fromIndex, int toIndex);
}
