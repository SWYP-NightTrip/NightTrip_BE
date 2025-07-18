package com.nighttrip.core.domain.triporder.repository;

import com.nighttrip.core.domain.triporder.entity.TripOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TripOrderRepository extends JpaRepository<TripOrder, Long> {

    @Query("""
            select MAX(t.order)
                        from TripOrder t 
                                    join TripDay td
                                                on td.tripPlan.title=:tripPlanTitle and td.order=:tripDayOrder
                        where t.tripDay.id=:tripDayId
            """)
    Optional<Integer> findLastOrder(@Param("tripPlanTitle") String tripPlanTitle, @Param("tripDayOrder") Integer tripDayOrder);
}
