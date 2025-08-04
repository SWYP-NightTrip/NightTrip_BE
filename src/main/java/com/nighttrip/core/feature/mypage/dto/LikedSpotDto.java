package com.nighttrip.core.feature.mypage.dto;

import com.nighttrip.core.domain.touristspot.entity.TouristSpot;
import lombok.Getter;

@Getter
public class LikedSpotDto {

    private final Long spotId;
    private final String spotName;
    private final String address;
    private final String category;
    private final String imageUrl;

    private LikedSpotDto(TouristSpot spot, String imageUrl) {
        this.spotId = spot.getId();
        this.spotName = spot.getSpotName();
        this.address = spot.getAddress();
        this.category = (spot.getCategory() != null) ? spot.getCategory().getKoreanName() : null;
        this.imageUrl = imageUrl;
    }

    public static LikedSpotDto from(TouristSpot spot,String imageUrl) {
        return new LikedSpotDto(spot, imageUrl);
    }

}
