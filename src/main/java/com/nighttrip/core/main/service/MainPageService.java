package com.nighttrip.core.main.service;

import com.nighttrip.core.domain.city.entity.City;
import com.nighttrip.core.domain.touristspot.dto.TouristSpotWithDistance;
import com.nighttrip.core.domain.touristspot.entity.TouristSpot;
import com.nighttrip.core.domain.touristspot.entity.TouristSpotReview;
import com.nighttrip.core.domain.touristspot.repository.TouristSpotRepository;
import com.nighttrip.core.domain.tripday.entity.TripDay;
import com.nighttrip.core.domain.tripplan.entity.TripPlan;
import com.nighttrip.core.domain.tripplan.repository.TripPlanRepository;
import com.nighttrip.core.domain.user.entity.User;
import com.nighttrip.core.global.enums.TripStatus;
import com.nighttrip.core.main.dto.RecommendedSpotDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MainPageService {

    private final TouristSpotRepository touristSpotRepository;
    private final TripPlanRepository tripPlanRepository;

    private static final int SPOT_COUNT = 10;

    private static final double DISTANCE_WEIGHT = 0.50;
    private static final double MAIN_WEIGHT_FOR_DISTANCE = 0.35;
    private static final double REVIEW_WEIGHT_FOR_DISTANCE = 0.15;

    private static final double MAIN_WEIGHT_NO_DISTANCE = 0.70;
    private static final double REVIEW_WEIGHT_NO_DISTANCE = 0.30;


    public List<RecommendedSpotDto> getNightPopularSpots(User user, Double userLat, Double userLon) {

        // 1. [최우선] 여행 계획 확인
        Optional<TripPlan> activePlanOpt = tripPlanRepository.findFirstByUserAndStatusInOrderByStartDateAsc(user, List.of(TripStatus.UPCOMING, TripStatus.ONGOING));

        if (activePlanOpt.isPresent()) {
            City targetCity = findTargetCityFromPlan(activePlanOpt.get());
            if (targetCity != null) {
                // 1-a. 여행 계획이 있고, 위치 정보도 있는 경우: 해당 도시 내에서 정교한 추천
                if (userLat != null && userLon != null) {
                    List<TouristSpotWithDistance> projections = touristSpotRepository.findSpotsInCityWithScores(
                            targetCity.getId(), userLat, userLon,
                            DISTANCE_WEIGHT, MAIN_WEIGHT_FOR_DISTANCE, REVIEW_WEIGHT_FOR_DISTANCE,
                            SPOT_COUNT
                    );
                    return projections.stream().map(RecommendedSpotDto::new).collect(Collectors.toList());
                }
                // 1-b. 여행 계획은 있지만, 위치 정보는 없는 경우: 해당 도시 내에서 인기/리뷰 점수 기반 추천
                else {
                    List<TouristSpot> spots = touristSpotRepository.findSpotsInCityByScoresWithoutLocation(
                            targetCity.getId(),
                            MAIN_WEIGHT_NO_DISTANCE,
                            REVIEW_WEIGHT_NO_DISTANCE,
                            SPOT_COUNT
                    );
                    return spots.stream().map(RecommendedSpotDto::new).collect(Collectors.toList());
                }
            }
        }

        // 2. 여행 계획이 없는 일반 사용자
        // 2-a. 위치 정보가 있는 경우: 전국 70km 반경 내에서 정교한 추천
        if (userLat != null && userLon != null) {
            List<TouristSpotWithDistance> projections = touristSpotRepository.findNearbyPopularSpots(
                    userLat, userLon,
                    DISTANCE_WEIGHT, MAIN_WEIGHT_FOR_DISTANCE, REVIEW_WEIGHT_FOR_DISTANCE,
                    SPOT_COUNT);
            return projections.stream().map(RecommendedSpotDto::new).collect(Collectors.toList());
        }
        // 2-b. 위치 정보가 없는 경우 (최종 폴백): 전국 단위로 인기/리뷰 점수 기반 추천
        else {
            List<TouristSpot> spots = touristSpotRepository.findSpotsByScoresWithoutLocation(
                    MAIN_WEIGHT_NO_DISTANCE,
                    REVIEW_WEIGHT_NO_DISTANCE,
                    SPOT_COUNT
            );
            return spots.stream().map(RecommendedSpotDto::new).collect(Collectors.toList());
        }
    }







    private City findTargetCityFromPlan(TripPlan activePlan) {
        LocalDate today = LocalDate.now();
        Optional<TripDay> currentTripDayOpt = activePlan.getTripDays().stream()
                .filter(day -> {
                    LocalDate tripDate = activePlan.getStartDate().plusDays(day.getDayOrder() - 1);
                    return tripDate.equals(today);
                }).findFirst();

        if (currentTripDayOpt.isPresent() && !currentTripDayOpt.get().getCities().isEmpty()) {
            return currentTripDayOpt.get().getCities().get(0);
        } else {
            return activePlan.getTripDays().stream()
                    .filter(day -> {
                        LocalDate tripDate = activePlan.getStartDate().plusDays(day.getDayOrder() - 1);
                        return !tripDate.isBefore(today) && !day.getCities().isEmpty();
                    })
                    .findFirst()
                    .map(day -> day.getCities().get(0))
                    .orElse(null);
        }
    }

    private RecommendedSpotDto convertToDtoWithoutDistance(TouristSpot spot) {
        return new RecommendedSpotDto(new TouristSpotWithDistance() {
            @Override public Long getId() { return spot.getId(); }
            @Override public String getSpotName() { return spot.getSpotName(); }
            @Override public String getCategory() { return spot.getCategory(); }
            @Override public String getAddress() { return spot.getAddress(); }
            @Override public City getCity() { return spot.getCity(); }
            @Override public List<TouristSpotReview> getTouristSpotReviews() { return spot.getTouristSpotReviews(); }
            @Override public Double getDistance() { return null; }
        });
    }
}
