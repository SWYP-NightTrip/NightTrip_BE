package com.nighttrip.core.feature.mypage.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MyPageResponseDto {
    private String userName;
    private String userAvatarUrl;
    private int level;
    private long bookmarkedSpotsCount;
    private long likedSpotsCount;
    private List<RecentPlanDto> recentPlans;
}
