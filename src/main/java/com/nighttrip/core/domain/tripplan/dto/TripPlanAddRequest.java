package com.nighttrip.core.domain.tripplan.dto;

import java.time.LocalDate;

public record TripPlanAddRequest(
        Long tripPlanId,
        Long touristSpotId,
        LocalDate visitedDate
        ) {
}
