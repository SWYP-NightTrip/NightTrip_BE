package com.nighttrip.core.domain.favoritespot.repository;

import com.nighttrip.core.domain.favoritespot.entity.UserSpot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteSpotRepository extends JpaRepository<UserSpot, Long> {

    boolean existsByUserIdAndSpotName(Long userId, String spotName);

    List<UserSpot> findByUserId(Long userId);
}

