package com.nighttrip.core.domain.tripplan.dto;

import com.nighttrip.core.global.enums.SpotCategory;
import com.nighttrip.core.global.enums.SpotDetails;

import java.util.EnumSet;
import java.util.List;

public record TouristSpotDetailResponse(
        Long id,
        String spotName,
        Double longitude,
        Double latitude,
        Integer checkCount,
        String address,
        String link,
        SpotCategory category,
        String spotDescription,
        String telephone,
        Integer mainWeight,
        Integer subWeight,
        List<String> hashTags,
        EnumSet<SpotDetails> touristSpotDetails
) {
}