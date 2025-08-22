package com.nighttrip.core.domain.tripplan.service.impl;

import com.nighttrip.core.domain.touristspot.dto.TouristSpotDetailResponse;
import com.nighttrip.core.domain.touristspot.entity.TouristSpot;
import com.nighttrip.core.domain.tripday.entity.TripDay;
import com.nighttrip.core.domain.triporder.entity.TripOrder;

import com.nighttrip.core.domain.tripplan.entity.TripPlan;
import com.nighttrip.core.domain.tripplan.repository.TripPlanRepository;
import com.nighttrip.core.domain.tripplan.service.TripPlanService;
import com.nighttrip.core.domain.user.entity.User;
import com.nighttrip.core.domain.user.repository.UserRepository;
import com.nighttrip.core.global.enums.ErrorCode;
import com.nighttrip.core.global.enums.TripStatus;
import com.nighttrip.core.global.exception.BusinessException;
import com.nighttrip.core.global.oauth.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.nighttrip.core.domain.tripplan.dto.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripPlanServiceImpl implements TripPlanService {
    private final TripPlanRepository tripPlanRepository;
    private final UserRepository userRepository;
    @Override
    public void changePlanStatus(TripPlanStatusChangeRequest request, Long planId) {
        TripPlan tripPlan = tripPlanRepository.findById(planId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRIP_PLAN_NOT_FOUND));

        TripStatus tripStatus = request.status();

        tripPlan.changeStatus(tripStatus);
        tripPlanRepository.save(tripPlan);
    }
    /**
     * 현재 수정 중인 여행 계획 목록을 페이지 단위로 조회합니다.
     * (UPCOMING, ONGOING 상태 포함)
     */
    public Page<TripPlanResponse> getOngoingTripPlans(Pageable pageable) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자를 찾을 수 없습니다."));

        List<TripStatus> statuses = List.of(TripStatus.UPCOMING, TripStatus.ONGOING);

        return tripPlanRepository.findByUser_IdAndStatusIn(user.getId(), statuses, pageable)
                .map(TripPlanResponse::from);
    }

    /**
     * 과거에 다녀온 여행 계획 목록을 페이지 단위로 조회합니다.
     * (COMPLETED 상태 포함)
     */
    public Page<TripPlanResponse> getPastTripPlans(Pageable pageable) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자를 찾을 수 없습니다."));

        return tripPlanRepository.findByUser_IdAndStatus(user.getId(), TripStatus.COMPLETED, pageable)
                .map(TripPlanResponse::from);
    }

/*
    public TripPlanDetailResponse getTripPlanDetails(Long tripPlanId) {
        TripPlan tripPlan = tripPlanRepository.findByIdWithAllDetails(tripPlanId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRIP_PLAN_NOT_FOUND));

        List<TripDayDetailResponse> tripDays = tripPlan.getTripDays().stream()
                .map(this::mapToTripDayDetailResponse)
                .collect(Collectors.toList());

        return new TripPlanDetailsResponse(
                tripPlan.getTitle(),
                tripPlan.getStartDate(),
                tripPlan.getEndDate(),
                tripDays
        );
    }

    private TripDayDetailResponse mapToTripDayDetailResponse(TripDay tripDay) {
        List<CityResponse> cities = tripDay.getCityOnTripDays().stream()
                .map(cityOnTripDay -> new CityResponse(
                        cityOnTripDay.getCity().getId(),
                        cityOnTripDay.getCity().getCityName()))
                .collect(Collectors.toList());

        List<TripOrderResponse> tripOrders = tripDay.getTripOrders().stream()
                .map(this::mapToTripOrderResponse)
                .collect(Collectors.toList());

        return new TripDayDetailResponse(
                tripDay.getId(),
                tripDay.getDayOrder(),
                cities,
                tripOrders
        );
    }

    private TripOrderResponse mapToTripOrderResponse(TripOrder tripOrder) {
        TouristSpot touristSpotEntity = tripOrder.getTouristSpot();
        TouristSpotDetailResponse touristSpot = null;
        if (touristSpotEntity != null) {
            touristSpot = new TouristSpotDetailResponse(
                    touristSpotEntity.getId(),
                    touristSpotEntity.getSpotName(),
                    touristSpotEntity.getLongitude(),
                    touristSpotEntity.getLatitude(),
                    touristSpotEntity.getCheckCount(),
                    touristSpotEntity.getAddress(),
                    touristSpotEntity.getLink(),
                    touristSpotEntity.getCategory(),
                    touristSpotEntity.getSpotDescription(),
                    touristSpotEntity.getTelephone(),
                    touristSpotEntity.getMainWeight(),
                    touristSpotEntity.getSubWeight(),
                    touristSpotEntity.getHashTagsAsList(),
                    touristSpotEntity.getTouristSpotDetails()
            );
        }

        return new TripOrderResponse(
                tripOrder.getId(),
                tripOrder.getOrderIndex(),
                tripOrder.getArrivalTime(),
                touristSpot
        );
    }*/
}
