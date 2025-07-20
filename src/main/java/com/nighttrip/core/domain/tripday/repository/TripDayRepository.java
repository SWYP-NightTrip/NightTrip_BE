package com.nighttrip.core.domain.tripday.repository;

import com.nighttrip.core.domain.tripday.entity.TripDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TripDayRepository extends JpaRepository<TripDay, Long> {

    @Query("select td from TripDay td where td.tripPlan.id=:tripPlanId and td.dayOrder=:tripDayId")
    Optional<TripDay> findByTripPlanIdAndTripDayId(Long tripPlanId, Integer tripDayId);
}
