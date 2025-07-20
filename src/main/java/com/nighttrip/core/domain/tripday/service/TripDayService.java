package com.nighttrip.core.domain.tripday.service;

import com.nighttrip.core.domain.tripday.dto.TripPlanChangeOrderRequest;
import com.nighttrip.core.domain.tripday.entity.TripDay;
import com.nighttrip.core.domain.tripday.repository.TripDayRepository;
import com.nighttrip.core.domain.tripplan.entity.TripPlan;
import com.nighttrip.core.global.enums.ErrorCode;
import com.nighttrip.core.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TripDayService {

    private final TripDayRepository tripDayRepository;

//    public void addPlan(TripPlan tripPlan, LocalDate visitedDate) {
//        TripDay tripDay = tripDayRepository.findByTripPlanIdAndTripDayId(tripPlan.getId(), tripDayId)
//                .orElseThrow(() -> new BusinessException(ErrorCode.TRIP_DAY_NOT_FOUND));
//
//        tripDay.changeTripOrder(request.from_index(), request.to_index());
//    }

    public void changePlanOrder(TripPlanChangeOrderRequest request, Long tripPlanId, Integer tripDayId) {
        TripDay tripDay = tripDayRepository.findByTripPlanIdAndTripDayId(tripPlanId, tripDayId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRIP_DAY_NOT_FOUND));

        tripDay.changeTripOrder(request.from_index(), request.to_index());
    }


}
