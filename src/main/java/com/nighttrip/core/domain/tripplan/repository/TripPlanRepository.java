package com.nighttrip.core.domain.tripplan.repository;

import com.nighttrip.core.domain.tripplan.entity.TripPlan;
import com.nighttrip.core.domain.user.entity.User;
import com.nighttrip.core.global.enums.TripStatus;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TripPlanRepository extends JpaRepository<TripPlan, Long> {

    Optional<TripPlan> findFirstByUserAndStatusInOrderByStartDateAsc(User user, List<TripStatus> statuses);

    Optional<TripPlan> findTopByUserAndStatusInOrderByUpdatedAtDesc(User user, List<TripStatus> statuses);
    List<TripPlan> findByUser_IdAndStatus(Long userId, TripStatus status);

    Page<TripPlan> findByUser_IdAndStatus(Long userId, TripStatus status, Pageable pageable);

    Page<TripPlan> findByUser_IdAndStatusIn(Long userId, List<TripStatus> statuses, Pageable pageable);

    @Query("SELECT tp FROM TripPlan tp " +
            "JOIN FETCH tp.tripDays td " +
            "JOIN FETCH td.tripOrders to " +
            "JOIN FETCH to.touristSpot " +
            "WHERE tp.id = :tripPlanId " +
            "ORDER BY td.dayOrder ASC, to.orderIndex ASC")
    Optional<TripPlan> findByIdWithDetails(@Param("tripPlanId") Long tripPlanId);
    List<TripPlan> findByUserId(Long userId);

    Optional<TripPlan> findByIdAndUserId(Long tripPlanId, Long id);

    List<TripPlan> findByUserAndStatusInOrderByNumIndexAsc(User user, List<TripStatus> statusGroup);
    

    List<TripPlan> findByUserAndStatusAndStartDateBefore(User user, TripStatus tripStatus, LocalDate today);

    List<TripPlan> findByUserAndStatusAndEndDateBefore(User user, TripStatus tripStatus, LocalDate today);

    Optional<TripPlan> findFirstByUserAndStatusOrderByNumIndexDesc(User user, TripStatus tripStatus);
}
