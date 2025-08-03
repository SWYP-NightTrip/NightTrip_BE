package com.nighttrip.core.feature.mypage.dto;

import com.nighttrip.core.domain.touristspot.entity.TouristSpot;
import com.nighttrip.core.domain.touristspot.entity.TouristSpotImageUri;
import lombok.Getter;

@Getter
public class LikedSpotDto {

    private final Long spotId;
    private final String spotName;
    private final String address;
    private final String category;
    private final String imageUrl;

    private LikedSpotDto(TouristSpot spot) {
        this.spotId = spot.getId();
        this.spotName = spot.getSpotName();
        this.address = spot.getAddress();
        this.category = (spot.getCategory() != null) ? spot.getCategory().getKoreanName() : null;
        this.imageUrl = extractMainImageUrl(spot);
    }

    public static LikedSpotDto from(TouristSpot spot) {
        return new LikedSpotDto(spot);
    }

    private String extractMainImageUrl(TouristSpot spot) {
        if (spot.getTouristSpotImageUris() == null || spot.getTouristSpotImageUris().isEmpty()) {
            return null;
        }

        return spot.getTouristSpotImageUris().stream()
                .filter(TouristSpotImageUri::isMain)
                .findFirst()
                .or(() -> spot.getTouristSpotImageUris().stream().findFirst())
                .map(TouristSpotImageUri::getUri)
                .orElse(null);
    }
}
