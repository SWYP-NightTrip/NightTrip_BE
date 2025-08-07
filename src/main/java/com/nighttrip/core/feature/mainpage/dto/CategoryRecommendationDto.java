package com.nighttrip.core.feature.mainpage.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nighttrip.core.global.enums.SpotCategory;
import lombok.Getter;

import java.util.List;

@Getter
public class CategoryRecommendationDto {

    private final String category;
    private final List<RecommendedSpotDto> spots;

    private final boolean isMore;
    private final String nickname;

    public CategoryRecommendationDto(SpotCategory category, List<RecommendedSpotDto> spots, boolean isMore, String nickname) {
        this.category = category.getKoreanName();
        this.spots = spots;
        this.isMore = isMore;
        this.nickname = nickname;
    }

    @JsonProperty("isMore")
    public boolean isMore() {
        return isMore;
    }
}
