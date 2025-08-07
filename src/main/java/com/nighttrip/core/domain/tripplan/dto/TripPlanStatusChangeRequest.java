package com.nighttrip.core.domain.tripplan.dto;

import com.nighttrip.core.global.enums.TripStatus;
import jakarta.validation.constraints.NotNull;

public record TripPlanStatusChangeRequest(
        @NotNull(message = "상태 값은 필수입니다.")
        TripStatus status
) {
}
