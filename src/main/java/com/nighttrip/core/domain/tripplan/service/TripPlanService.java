package com.nighttrip.core.domain.tripplan.service;

import com.nighttrip.core.domain.tripplan.dto.TripPlanAddRequest;
import com.nighttrip.core.domain.tripplan.dto.TripPlanStatusChangeRequest;
import com.nighttrip.core.domain.tripplan.entity.TripPlan;
import com.nighttrip.core.domain.tripplan.repository.TripPlanRepository;
import com.nighttrip.core.global.enums.ErrorCode;
import com.nighttrip.core.global.enums.TripStatus;
import com.nighttrip.core.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
@RequiredArgsConstructor
public class TripPlanService {
    private final TripPlanRepository tripPlanRepository;

// 일정선택 변경사항으로 수정 예정
//    public void addPlan(TripPlanAddRequest request) {
//        TripPlan tripPlan = tripPlanRepository.findById(request.tripPlanId())
//                .orElseThrow(() -> new BusinessException(ErrorCode.TRIP_PLAN_NOT_FOUND));
//        Integer dayOrder = getDayOrder(tripPlan.getStartDate(), request.visitedDate());
//        tripPlanRepository.save()
//    }
//
//    private Integer getDayOrder(LocalDate startDate, LocalDate visitedDate) {
//        int dis = (int) DAYS.between(startDate, visitedDate);
//        return dis + 1;
//    }

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
