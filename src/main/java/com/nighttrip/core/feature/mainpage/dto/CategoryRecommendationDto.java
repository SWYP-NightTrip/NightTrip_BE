package com.nighttrip.core.feature.mainpage.dto;

import com.nighttrip.core.global.enums.SpotCategory;
import lombok.Getter;

import java.util.List;

@Getter
public class CategoryRecommendationDto {

    private final String category; // 추천된 카테고리의 한글 이름
    private final List<RecommendedSpotDto> spots;

    public CategoryRecommendationDto(SpotCategory category, List<RecommendedSpotDto> spots) {
        this.category = category.getKoreanName();
        this.spots = spots;
    }
}
