package com.nighttrip.core.domain.userspot.dto;

import java.util.List;

public record UserSpotAddRequest(
        String placeName,
        String placeAddress,
        String placeExplain,
        List<String> imageUrl,
        List<String> category
) {
}
