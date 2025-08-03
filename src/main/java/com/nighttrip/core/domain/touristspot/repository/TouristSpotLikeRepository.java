package com.nighttrip.core.domain.touristspot.repository;

import com.nighttrip.core.domain.touristspot.entity.TourLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TouristSpotLikeRepository extends JpaRepository<TourLike, Long> {
    Boolean existsByTouristSpotId(Long touristSpotId);
}
