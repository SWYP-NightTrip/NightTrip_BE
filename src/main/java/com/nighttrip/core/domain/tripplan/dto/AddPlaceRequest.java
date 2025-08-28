package com.nighttrip.core.domain.tripplan.dto;


import java.util.List;

public record AddPlaceRequest(Long tripPlanId, Integer tripDayId, List<Long> touristSpotIds) {
}