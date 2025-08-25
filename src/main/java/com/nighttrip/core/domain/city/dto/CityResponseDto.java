package com.nighttrip.core.domain.city.dto;

import com.nighttrip.core.domain.city.entity.City;
import com.nighttrip.core.global.dto.SearchDocument;

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

    public static CityResponseDto from(SearchDocument document) {

        Long cityId = Long.parseLong(document.getId().split("_")[1]);
        return new CityResponseDto(
                cityId,
                document.getName(),
                document.getImageUrl()
        );
    }
}
