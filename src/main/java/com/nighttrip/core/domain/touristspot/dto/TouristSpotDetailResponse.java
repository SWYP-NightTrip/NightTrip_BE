package com.nighttrip.core.domain.touristspot.dto;

import com.nighttrip.core.domain.touristspot.entity.TouristSpot;

public record TouristSpotDetailResponse(
        String spotName,
        String address,
        Integer checkCount,
        Integer mainWeight,
        Integer subWeight,
        String category,
        String link,
        String spotDescription,
        String telephone,
        Double latitude,
        Double longitude
) {
    public static TouristSpotDetailResponse fromEntity(TouristSpot touristSpot){
        return new TouristSpotDetailResponse(
                touristSpot.getSpotName(),
                touristSpot.getAddress(),
                touristSpot.getCheckCount(),
                touristSpot.getMainWeight(),
                touristSpot.getSubWeight(),
                touristSpot.getCategory().getKoreanName(),
                touristSpot.getLink(),
                touristSpot.getSpotDescription(),
                touristSpot.getTelephone(),
                touristSpot.getLatitude(),
                touristSpot.getLongitude()
        );
    }
}
