package com.nighttrip.core.domain.tripplan.repository;

import com.nighttrip.core.domain.tripplan.entity.TripPlan;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TripPlanRepository extends JpaRepository<TripPlan, Long> {

    @Query("select p from TripPlan p where p.title=:name")
    Optional<TripPlan> findByName(@Param("name") String name);
}
