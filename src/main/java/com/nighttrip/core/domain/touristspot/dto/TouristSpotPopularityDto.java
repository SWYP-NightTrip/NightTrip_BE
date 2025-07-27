package com.nighttrip.core.domain.touristspot.dto;

public record TouristSpotPopularityDto(
        Long spotId,
        String spotName,
        String imageUrl,
        String address,
        String category,
        String spotDescription,
        Long popularityScore
) {}