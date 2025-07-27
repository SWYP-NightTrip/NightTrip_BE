package com.nighttrip.core.domain.triporder.repository;

import com.nighttrip.core.domain.triporder.entity.TripOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface TripOrderRepository extends JpaRepository<TripOrder, Long> {

    @Query("""
                select max(t.orderIndex)
                from TripOrder t
                where t.tripDay.tripPlan.title = :tripPlanTitle
                  and t.tripDay.dayOrder = :tripDayOrder
            """)
    Optional<BigDecimal> findLastOrder(
            @Param("tripPlanTitle") String tripPlanTitle,
            @Param("tripDayOrder") Integer tripDayOrder
    );
}
