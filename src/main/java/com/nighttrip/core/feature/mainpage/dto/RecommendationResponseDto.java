package com.nighttrip.core.feature.mainpage.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class RecommendationResponseDto {

    private final List<RecommendedSpotDto> spots;
    private final boolean isMore;

    public RecommendationResponseDto(List<RecommendedSpotDto> spots, boolean isMore) {
        this.spots = spots;
        this.isMore = isMore;
    }

    @JsonProperty("isMore")
    public boolean isMore() {
        return isMore;
    }
}
