package com.nighttrip.core.domain.touristspot.repository;

import com.nighttrip.core.domain.touristspot.entity.TourLike;
import com.nighttrip.core.domain.touristspot.entity.TouristSpot;
import com.nighttrip.core.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TouristSpotLikeRepository extends JpaRepository<TourLike, Long> {
    Boolean existsByTouristSpotId(Long touristSpotId);

    Optional<TourLike> findByUserAndTouristSpot(User user, TouristSpot touristSpot);

    boolean existsByUserAndTouristSpot(User user, TouristSpot touristSpot);
}
