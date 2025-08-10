package com.nighttrip.core.domain.touristspot.dto;

import com.nighttrip.core.domain.touristspot.entity.TouristSpot;

import java.util.List;

public record TouristSpotDetailResponse(
        String spotName,
        String region,
        String address,
        Integer checkCount,
        Integer mainWeight,
        Integer subWeight,
        String category,
        String link,
        String spotDescription,
        String telephone,
        Double latitude,
        Double longitude,
        Double starAverage,
        Long starCountSum,
        Boolean isLiked,
        List<String> spotImages,
        List<SpotDetailsDto> spotDetails
) {
    public static TouristSpotDetailResponse fromEntity(TouristSpot touristSpot, Double avg, Long starCountSum, Boolean isLiked, List<String> imageUrls, List<SpotDetailsDto> spotDetails) {
        return new TouristSpotDetailResponse(
                touristSpot.getSpotName(),
                touristSpot.getAddress().split(" ")[0].endsWith("도") ? touristSpot.getAddress().split(" ")[0] +" "+ touristSpot.getAddress().split(" ")[1] : touristSpot.getAddress().split(" ")[0],
                touristSpot.getAddress(),
                touristSpot.getCheckCount(),
                touristSpot.getMainWeight(),
                touristSpot.getSubWeight(),
                touristSpot.getCategory().getKoreanName(),
                touristSpot.getLink(),
                touristSpot.getSpotDescription(),
                touristSpot.getTelephone(),
                touristSpot.getLatitude(),
                touristSpot.getLongitude(),
                avg,
                starCountSum,
                isLiked,
                imageUrls,
                spotDetails
        );
    }
}
