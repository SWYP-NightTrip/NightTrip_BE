package com.nighttrip.core.domain.touristspot.Implementation;

import com.nighttrip.core.domain.touristspot.dto.TouristSpotResponseDto;

import java.util.List;

public interface TouristSpotService
{
    List<TouristSpotResponseDto> getPopularTouristSpotsInCity(Long cityId);
    List<TouristSpotResponseDto> getRecommendedTouristSpotsInCity(Long cityId);
}
