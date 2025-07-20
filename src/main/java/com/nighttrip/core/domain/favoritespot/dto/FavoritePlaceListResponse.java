package com.nighttrip.core.domain.favoritespot.dto;

import com.nighttrip.core.domain.favoritespot.entity.UserSpot;

public record FavoritePlaceListResponse(
    String placeName,
    String ThumbnailUrl
) {
    public FavoritePlaceListResponse(UserSpot userSpot) {
        this(userSpot.getSpotName(), userSpot.getImageUrl());
    }
}
