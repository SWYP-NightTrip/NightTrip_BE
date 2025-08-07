package com.nighttrip.core.domain.tripplan.service.impl;

import com.nighttrip.core.domain.tripplan.dto.TripPlanStatusChangeRequest;
import com.nighttrip.core.domain.tripplan.entity.TripPlan;
import com.nighttrip.core.domain.tripplan.repository.TripPlanRepository;
import com.nighttrip.core.domain.tripplan.service.TripPlanService;
import com.nighttrip.core.global.enums.ErrorCode;
import com.nighttrip.core.global.enums.TripStatus;
import com.nighttrip.core.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TripPlanServiceImpl implements TripPlanService {
    private final TripPlanRepository tripPlanRepository;

    @Override
    public void changePlanStatus(TripPlanStatusChangeRequest request, Long planId) {
        TripPlan tripPlan = tripPlanRepository.findById(planId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRIP_PLAN_NOT_FOUND));

        TripStatus tripStatus = request.status();

        tripPlan.changeStatus(tripStatus);
        tripPlanRepository.save(tripPlan);
    }
}
