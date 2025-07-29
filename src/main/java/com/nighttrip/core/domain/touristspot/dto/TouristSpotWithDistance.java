package com.nighttrip.core.domain.touristspot.dto;

import com.nighttrip.core.domain.city.entity.City;
import com.nighttrip.core.domain.touristspot.entity.TouristSpotReview;

import java.util.List;

public interface TouristSpotWithDistance {

    Long getId();
    String getSpotName();
    String getCategory();
    String getAddress();

    City getCity();
    List<TouristSpotReview> getTouristSpotReviews();

    Double getDistance();
}
