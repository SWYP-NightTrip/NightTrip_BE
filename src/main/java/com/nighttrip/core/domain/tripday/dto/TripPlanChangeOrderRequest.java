package com.nighttrip.core.domain.tripday.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TripPlanChangeOrderRequest(
        @NotNull(message = "이동할 시작 위치는 필수입니다.")
        @Min(value = 0, message = "fromIndex는 0 이상이어야 합니다.")
        Integer fromIndex,

        @NotNull(message = "이동할 목표 위치는 필수입니다.")
        @Min(value = 0, message = "toIndex는 0 이상이어야 합니다.")
        Integer toIndex
) {
}
