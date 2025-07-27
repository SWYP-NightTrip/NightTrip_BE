package com.nighttrip.core.domain.touristspot.dto;

import com.nighttrip.core.domain.city.entity.City;
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
}
