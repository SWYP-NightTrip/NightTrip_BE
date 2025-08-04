package com.nighttrip.core.domain.city.dto;


public record CityPopularityDto(
        Long id,
        String cityName,
        Long totalPopularityScore
) {}