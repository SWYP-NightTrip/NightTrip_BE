package com.nighttrip.core.domain.tripplan.service;

import com.nighttrip.core.domain.tripplan.dto.TripPlanDetailResponse;
import com.nighttrip.core.domain.tripplan.dto.TripPlanReorderRequest;
import com.nighttrip.core.domain.tripplan.dto.TripPlanResponse;
import com.nighttrip.core.domain.tripplan.dto.TripPlanStatusChangeRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TripPlanService {
    void changePlanStatus(TripPlanStatusChangeRequest request, Long planId);
    public Page<TripPlanResponse> getPastTripPlans(Pageable pageable);
    public Page<TripPlanResponse> getOngoingTripPlans(Pageable pageable);
    void deleteTripPlan(Long tripPlanId);
    void reorderTripPlan(TripPlanReorderRequest request);
    void updateTripPlanStatusesForUser();

}
