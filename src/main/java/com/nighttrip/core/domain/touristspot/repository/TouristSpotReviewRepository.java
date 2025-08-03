package com.nighttrip.core.domain.touristspot.repository;

import com.nighttrip.core.domain.touristspot.entity.TouristSpotReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TouristSpotReviewRepository extends JpaRepository<TouristSpotReview, Long> {
    List<TouristSpotReview> findByTouristSpotId(Long touristSpotId);
}
