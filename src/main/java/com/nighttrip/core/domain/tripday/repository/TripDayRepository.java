package com.nighttrip.core.domain.tripday.repository;

import com.nighttrip.core.domain.tripday.entity.TripDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TripDayRepository extends JpaRepository<TripDay, Long> {

    @Query("select td from TripDay td where td.tripPlan.title=:tripPlanName and td.order=:tripDayOrder")
    Optional<TripDay> findByTripPlanNameAndDayOrder(@Param("tripPlanName") String tripPlanName, @Param("tripDayOrder") Integer tripDayOrder);
}
