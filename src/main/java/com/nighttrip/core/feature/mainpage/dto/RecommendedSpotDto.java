package com.nighttrip.core.feature.mainpage.dto;

import com.nighttrip.core.domain.city.entity.City;
import com.nighttrip.core.domain.touristspot.dto.TouristSpotWithDistance;
import com.nighttrip.core.domain.touristspot.entity.TouristSpot;
import com.nighttrip.core.domain.touristspot.entity.TouristSpotImageUri;
import com.nighttrip.core.domain.touristspot.entity.TouristSpotReview;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class RecommendedSpotDto {
    private Long id;
    private String name;
    private String stars;
    private int reviewCount;
    private String location;
    private String imgUrl;
    private Double distanceKm;

    public RecommendedSpotDto(TouristSpotWithDistance projection) {
        this.id = projection.getId();
        this.name = projection.getSpotName();

        City city = projection.getCity();
        if (city != null) {
            this.location = city.getCityName();
            this.imgUrl = city.getImageUrl();
        }

        List<TouristSpotReview> reviews = projection.getTouristSpotReviews();
        this.reviewCount = (reviews != null) ? reviews.size() : 0;

        if (this.reviewCount > 0) {
            double averageStars = reviews.stream()
                    .mapToInt(TouristSpotReview::getScope)
                    .average()
                    .orElse(0.0);
            this.stars = String.format("%.1f", averageStars);
        } else {
            this.stars = "0.0";
        }

        if (projection.getDistance() != null) {
            this.distanceKm = Math.round(projection.getDistance() * 10.0) / 10.0;
        } else {
            this.distanceKm = null;
        }
    }

    public RecommendedSpotDto(TouristSpot spot) {
        this.id = spot.getId();
        this.name = spot.getSpotName();

        if (spot.getCity() != null) {
            this.location = spot.getCity().getCityName();
        }

        this.imgUrl = spot.getTouristSpotImageUris().stream()
                .filter(TouristSpotImageUri::isMain)
                .map(TouristSpotImageUri::getUri)
                .findFirst()
                .orElse(null);

        List<TouristSpotReview> reviews = spot.getTouristSpotReviews();
        this.reviewCount = reviews.size();
        if (this.reviewCount > 0) {
            double averageStars = reviews.stream()
                    .mapToInt(TouristSpotReview::getScope)
                    .average().orElse(0.0);
            this.stars = String.format("%.1f", averageStars);
        } else {
            this.stars = "0.0";
        }

        // 이 생성자는 거리 정보가 없는 경우에 호출되므로 distanceKm는 null입니다.
        this.distanceKm = null;
    }
}
