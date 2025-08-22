package com.nighttrip.core.domain.triporder.repository;

import com.nighttrip.core.domain.triporder.entity.TripOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

public interface TripOrderRepository extends JpaRepository<TripOrder, Long> {

    @Query("SELECT MAX(to.orderIndex) FROM TripOrder to " +
            "WHERE to.tripDay.tripPlan.id = :tripPlanId " +
            "AND to.tripDay.dayOrder = :tripDayId")
    Optional<Long> findLastOrder(@Param("tripPlanId") Long tripPlanId, @Param("tripDayId") Integer tripDayId);


    @Modifying
    @Transactional
    @Query("UPDATE TripOrder to SET to.orderIndex = to.orderIndex - 1 " +
            "WHERE to.tripDay.id = :tripDayId AND to.orderIndex >= :fromIndex AND to.orderIndex <= :toIndex")
    void decrementOrderIndex(@Param("tripDayId") Long tripDayId, @Param("fromIndex") int fromIndex, @Param("toIndex") int toIndex);

    @Modifying
    @Transactional
    @Query("UPDATE TripOrder to SET to.orderIndex = to.orderIndex + 1 " +
            "WHERE to.tripDay.id = :tripDayId AND to.orderIndex >= :toIndex AND to.orderIndex <= :fromIndex")
    void incrementOrderIndex(@Param("tripDayId") Long tripDayId, @Param("toIndex") int toIndex, @Param("fromIndex") int fromIndex);
}
