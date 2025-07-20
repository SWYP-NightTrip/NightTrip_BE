package com.nighttrip.core.domain.touristspot.repository;

import com.nighttrip.core.domain.touristspot.entity.TouristSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface TouristSpotRepository extends JpaRepository<TouristSpot, Long> {

    @Query("""
                select max(t.orderIndex)
                from TripOrder t
                where t.tripDay.tripPlan.id=:tripPlanId
                and t.tripDay.dayOrder=:tripDayOrder
            """)
    Optional<BigDecimal> findLastOrder(@Param("tripPlanId") Long tripPlanId, @Param("tripDayOrder") Integer tripDayOrder);

}
