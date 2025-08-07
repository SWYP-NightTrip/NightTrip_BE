package com.nighttrip.core.domain.userspot.dto;

import com.nighttrip.core.domain.userspot.entity.UserSpot;

import java.util.List;

public record UserSpotListResponse(
    String placeName,
    String placeAddress,
    String placeMemo,
    String category,
    String ThumbnailUrl
) {
    public UserSpotListResponse(UserSpot userSpot, String imageUrl) {
        this(userSpot.getSpotName(), userSpot.getAddress(),userSpot.getAddress(),userSpot.getCategory().getKoreanName(), imageUrl);
    }
}
