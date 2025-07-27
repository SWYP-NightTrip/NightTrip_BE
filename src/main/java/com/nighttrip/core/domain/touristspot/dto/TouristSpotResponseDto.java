package com.nighttrip.core.domain.touristspot.dto;


public record TouristSpotResponseDto(
        Long id,
        String spotName,
        String address,
        String category,
        String spotDescription,
        String imageUrl
) {}