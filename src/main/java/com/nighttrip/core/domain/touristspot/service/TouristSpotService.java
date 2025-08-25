package com.nighttrip.core.domain.touristspot.service;

import com.nighttrip.core.domain.touristspot.dto.TouristSpotDetailResponse;
import com.nighttrip.core.domain.touristspot.dto.TouristSpotResponseDto;

import java.util.List;

public interface TouristSpotService
{
    List<TouristSpotResponseDto> getPopularTouristSpotsInCity(Long cityId);
    List<TouristSpotResponseDto> getRecommendedTouristSpotsInCity(Long cityId);

    TouristSpotDetailResponse getTouristSpotDetail(Long touristSpotId);

    void addLike(Long touristSpotId);
    List<TouristSpotResponseDto> searchTouristSpots(String keyword);
    double calculateDistanceBetweenSpots(Long spotOneId, Long spotTwoId);
}
