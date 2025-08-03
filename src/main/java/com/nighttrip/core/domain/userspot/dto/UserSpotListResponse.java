package com.nighttrip.core.domain.userspot.dto;

import com.nighttrip.core.domain.userspot.entity.UserSpot;

public record UserSpotListResponse(
    String placeName,
    String ThumbnailUrl
) {
    public UserSpotListResponse(UserSpot userSpot, String imageUrl) {
        this(userSpot.getSpotName(), imageUrl);
    }
}
