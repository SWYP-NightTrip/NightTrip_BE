package com.nighttrip.core.domain.touristspot.dto;

public record RecommendTouristSpotResponse(
        Long id,
        int rank,
        String reason,
        String spotName,
        String address,
        String category,
        Double starAvg,
        Long reviewCount,
        String imageUrl
) {}
