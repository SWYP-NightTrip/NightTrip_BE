package com.nighttrip.core.domain.tripplan.service;

import com.nighttrip.core.domain.tripplan.dto.TripPlanStatusChangeRequest;
import com.nighttrip.core.domain.tripplan.entity.TripPlan;
import com.nighttrip.core.domain.tripplan.repository.TripPlanRepository;
import com.nighttrip.core.global.enums.ErrorCode;
import com.nighttrip.core.global.enums.TripStatus;
import com.nighttrip.core.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TripPlanService {
    private final TripPlanRepository tripPlanRepository;

    public void changePlanStatus(TripPlanStatusChangeRequest request, Long planId) {
        TripPlan tripPlan = tripPlanRepository.findById(planId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRIP_PLAN_NOT_FOUND));

        String upperCaseStatus = request.status().toUpperCase();
        if (!EnumUtils.isValidEnum(TripStatus.class, upperCaseStatus)) {
            throw new BusinessException(ErrorCode.INVALID_TRIP_STATUS);
        }

        tripPlan.changeStatus(TripStatus.valueOf(upperCaseStatus));
        tripPlanRepository.save(tripPlan);
    }
}
