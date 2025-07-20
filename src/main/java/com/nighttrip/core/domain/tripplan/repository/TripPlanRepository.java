package com.nighttrip.core.domain.tripplan.repository;

import com.nighttrip.core.domain.tripplan.entity.TripPlan;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TripPlanRepository extends JpaRepository<TripPlan, Long> {

}
