package com.nighttrip.core.domain.touristspot.repository;

import com.nighttrip.core.domain.touristspot.entity.TourLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TouristSpotLIkeRepository extends JpaRepository<TourLike, Long> {
}
