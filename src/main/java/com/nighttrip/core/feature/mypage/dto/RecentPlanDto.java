package com.nighttrip.core.feature.mypage.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class RecentPlanDto {
    private Long planId;
    private String planTitle;
    private String planPhotoUrl;
    private LocalDate startDate;
    private LocalDate endDate;
}
