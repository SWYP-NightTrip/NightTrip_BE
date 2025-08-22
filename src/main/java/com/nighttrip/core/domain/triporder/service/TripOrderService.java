package com.nighttrip.core.domain.triporder.service;

public interface TripOrderService {

    void updateArrivalTime(Long tripOrderId, String arrivalTime);

    void deleteTripOrder(Long tripOrderId);
    void addPlace(Long tripPlanId, Integer tripDayId, Long touristSpotId);
    void moveTripOrder(Long movingTripOrderId, Long originalTripDayId, Long destinationTripDayId, int fromIndex, int toIndex);
}
