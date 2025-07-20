package com.nighttrip.core.domain.favoritespot.dto;

import java.util.List;

public record FavoritePlaceAddRequest(
        String placeName,
        String placeAddress,
        String placeExplain,
        String imageUrl,
        List<String> category
) {
}
