package com.nighttrip.core.domain.tripplan.repository;

import com.nighttrip.core.domain.tripplan.entity.TripPlan;
import com.nighttrip.core.domain.user.entity.User;
import com.nighttrip.core.global.enums.TripStatus;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TripPlanRepository extends JpaRepository<TripPlan, Long> {

    Optional<TripPlan> findFirstByUserAndStatusInOrderByStartDateAsc(User user, List<TripStatus> statuses);

    Optional<TripPlan> findTopByUserAndStatusInOrderByUpdatedAtDesc(User user, List<TripStatus> statuses);
}
