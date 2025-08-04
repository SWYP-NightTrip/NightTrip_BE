package com.nighttrip.core.domain.userspot.repository;

import com.nighttrip.core.domain.userspot.entity.UserSpot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSpotRepository extends JpaRepository<UserSpot, Long> {

    boolean existsByUserIdAndSpotName(Long userId, String spotName);

    List<UserSpot> findByUserId(Long userId);
}

