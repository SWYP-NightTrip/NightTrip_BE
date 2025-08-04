package com.nighttrip.core.domain.city.dto;

import com.nighttrip.core.domain.city.entity.City;

public record CityResponseDto(
        Long id,
        String cityName,
        String imageUrl
) {
    public static CityResponseDto from(City city, String imageUrl) {
        return new CityResponseDto(
                city.getId(),
                city.getCityName(),
                imageUrl
        );
    }
}
