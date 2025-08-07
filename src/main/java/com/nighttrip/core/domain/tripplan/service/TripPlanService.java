package com.nighttrip.core.domain.tripplan.service;

import com.nighttrip.core.domain.tripplan.dto.TripPlanStatusChangeRequest;

public interface TripPlanService {
    void changePlanStatus(TripPlanStatusChangeRequest request, Long planId);
}
