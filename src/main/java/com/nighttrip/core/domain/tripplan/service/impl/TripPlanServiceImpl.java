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
import org.springframework.data.domain.Pageable;import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.nighttrip.core.domain.tripplan.dto.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
                .orElseThrow(() ->  new BusinessException(ErrorCode.USER_NOT_FOUND));

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
                .orElseThrow(() ->  new BusinessException(ErrorCode.USER_NOT_FOUND));

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

    public void deleteTripPlan(Long tripPlanId) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        TripPlan tripPlan = tripPlanRepository.findByIdAndUserId(tripPlanId, user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TRIP_PLAN_NOT_FOUND));

        tripPlanRepository.delete(tripPlan);
    }

    public void reorderTripPlan(TripPlanReorderRequest request) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        TripPlan movingPlan = tripPlanRepository.findById(request.tripPlanId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TRIP_PLAN_NOT_FOUND));
        if (!movingPlan.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }

        List<TripStatus> statusGroup;
        if (movingPlan.getStatus() == TripStatus.COMPLETED) {
            statusGroup = Arrays.asList(TripStatus.COMPLETED);
        } else {
            statusGroup = Arrays.asList(TripStatus.UPCOMING, TripStatus.ONGOING);
        }

        List<TripPlan> tripPlans = tripPlanRepository.findByUserAndStatusInOrderByNumIndexAsc(user, statusGroup);

        List<TripPlan> plansToUpdate;

        if (request.fromIndex() < request.toIndex()) {
            plansToUpdate = tripPlans.stream()
                    .filter(p -> p.getNumIndex() > request.fromIndex() && p.getNumIndex() <= request.toIndex())
                    .peek(p -> p.changeNumIndex(p.getNumIndex() - 1))
                    .collect(Collectors.toList());
        } else if (request.fromIndex() > request.toIndex()) {
            plansToUpdate = tripPlans.stream()
                    .filter(p -> p.getNumIndex() >= request.toIndex() && p.getNumIndex() < request.fromIndex())
                    .peek(p -> p.changeNumIndex(p.getNumIndex() + 1))
                    .collect(Collectors.toList());
        } else {
            return;
        }

        movingPlan.changeNumIndex(request.toIndex());
        plansToUpdate.add(movingPlan);
        tripPlanRepository.saveAll(plansToUpdate);
    }

    /**
     * 현재 유저의 여행 계획 상태를 업데이트합니다.
     */
    public void updateTripPlanStatusesForUser() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        LocalDate today = LocalDate.now();

        List<TripPlan> upcomingToOngoing = tripPlanRepository.findByUserAndStatusAndStartDateBefore(user, TripStatus.UPCOMING, today);
        for (TripPlan plan : upcomingToOngoing) {
            plan.changeStatus(TripStatus.ONGOING);
        }
        tripPlanRepository.saveAll(upcomingToOngoing);

        List<TripPlan> ongoingToCompleted = tripPlanRepository.findByUserAndStatusAndEndDateBefore(user, TripStatus.ONGOING, today);
        if (!ongoingToCompleted.isEmpty()) {
            Optional<TripPlan> lastCompletedPlan = tripPlanRepository.findFirstByUserAndStatusOrderByNumIndexDesc(user, TripStatus.COMPLETED);
            Long maxIndex = lastCompletedPlan.map(TripPlan::getNumIndex).orElse(0L);

            for (TripPlan plan : ongoingToCompleted) {
                maxIndex++;
                plan.changeStatus(TripStatus.COMPLETED);
                plan.changeNumIndex(maxIndex);
            }
            tripPlanRepository.saveAll(ongoingToCompleted);
        }
    }
}
