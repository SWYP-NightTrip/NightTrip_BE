package com.nighttrip.core.domain.tripplan.dto;



import com.nighttrip.core.domain.tripplan.entity.TripPlan;
import com.nighttrip.core.global.enums.TripStatus;

import java.time.LocalDate;

public record TripPlanResponse(
        Long id,
        String title,
        LocalDate startDate,
        LocalDate endDate,
        TripStatus status
) {
    public static TripPlanResponse from(TripPlan tripPlan) {
        return new TripPlanResponse(
                tripPlan.getId(),
                tripPlan.getTitle(),
                tripPlan.getStartDate(),
                tripPlan.getEndDate(),
                tripPlan.getStatus()
        );
    }
}