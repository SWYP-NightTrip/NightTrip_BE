package com.nighttrip.core.domain.tripplan.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record TripOrderMoveRequest(
        @NotNull Long movingTripOrderId,
        @NotNull Long originalTripDayId,
        @NotNull Long destinationTripDayId,
        @PositiveOrZero int fromIndex,
        @PositiveOrZero int toIndex
) {
}