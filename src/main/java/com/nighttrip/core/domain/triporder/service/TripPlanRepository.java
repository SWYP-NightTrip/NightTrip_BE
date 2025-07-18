package com.nighttrip.core.domain.triporder.service;

import com.nighttrip.core.domain.tripplan.entity.TripPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripPlanRepository  extends JpaRepository<TripPlan, Long> {
}
