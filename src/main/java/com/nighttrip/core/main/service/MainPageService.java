package com.nighttrip.core.main.service;

import com.nighttrip.core.domain.city.entity.City;
import com.nighttrip.core.domain.touristspot.dto.TouristSpotWithDistance;
import com.nighttrip.core.domain.touristspot.entity.TouristSpot;
import com.nighttrip.core.domain.touristspot.entity.TouristSpotReview;
import com.nighttrip.core.domain.touristspot.repository.TouristSpotRepository;
import com.nighttrip.core.domain.tripday.entity.TripDay;
import com.nighttrip.core.domain.tripplan.entity.TripPlan;
import com.nighttrip.core.domain.tripplan.repository.TripPlanRepository;
import com.nighttrip.core.domain.user.entity.BookMark;
import com.nighttrip.core.domain.user.entity.User;
import com.nighttrip.core.domain.user.repository.BookMarkRepository;
import com.nighttrip.core.global.enums.TripStatus;
import com.nighttrip.core.main.dto.RecommendedSpotDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MainPageService {

    private final TouristSpotRepository touristSpotRepository;
    private final TripPlanRepository tripPlanRepository;
    private final BookMarkRepository bookMarkRepository;

    private static final int SPOT_COUNT = 10;

    private static final double DISTANCE_WEIGHT = 0.50;
    private static final double MAIN_WEIGHT_FOR_DISTANCE = 0.35;
    private static final double REVIEW_WEIGHT_FOR_DISTANCE = 0.15;

    private static final double MAIN_WEIGHT_NO_DISTANCE = 0.70;
    private static final double REVIEW_WEIGHT_NO_DISTANCE = 0.30;

    private static final double CATEGORY_SUB_WEIGHT = 0.5;
    private static final double DISTANCE_WEIGHT_FOR_CAT = 0.5;


    public List<RecommendedSpotDto> getNightPopularSpots(User user, Double userLat, Double userLon) {

        if (user == null) {
            List<TouristSpot> spots = touristSpotRepository.findSpotsByScoresWithoutLocation(
                    MAIN_WEIGHT_NO_DISTANCE,
                    REVIEW_WEIGHT_NO_DISTANCE,
                    SPOT_COUNT);
            return spots.stream().map(RecommendedSpotDto::new).collect(Collectors.toList());
        }

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


    public List<RecommendedSpotDto> getCategoryRecommendedSpots(User user, Double userLat, Double userLon) {

        if (user == null) {
            List<String> allCategories = touristSpotRepository.findAllDistinctCategories();
            if (allCategories.isEmpty()) {
                return Collections.emptyList();
            }
            Random random = new Random();
            String favoriteCategory = allCategories.get(random.nextInt(allCategories.size()));

            List<TouristSpot> spots = touristSpotRepository.findByCategoryOrderBySubWeightDesc(favoriteCategory, PageRequest.of(0, SPOT_COUNT));
            return spots.stream().map(RecommendedSpotDto::new).collect(Collectors.toList());
        }

        // 1. 사용자의 북마크 기록을 바탕으로 가장 선호하는 카테고리를 찾습니다.
        String favoriteCategory = determineFavoriteCategory(user);
        if (favoriteCategory == null) {
            List<String> allCategories = touristSpotRepository.findAllDistinctCategories();

            if (allCategories.isEmpty()) {
                return Collections.emptyList();
            }

            Random random = new Random();
            favoriteCategory = allCategories.get(random.nextInt(allCategories.size()));
        }

        // 2. [최우선] 사용자가 진행 중이거나 예정된 여행 계획이 있는지 확인합니다.
        Optional<TripPlan> activePlanOpt = tripPlanRepository.findFirstByUserAndStatusInOrderByStartDateAsc(user, List.of(TripStatus.UPCOMING, TripStatus.ONGOING));
        if (activePlanOpt.isPresent()) {
            City targetCity = findTargetCityFromPlan(activePlanOpt.get());
            if (targetCity != null) {
                List<TouristSpot> spots = touristSpotRepository.findByCityAndCategoryOrderBySubWeightDesc(targetCity, favoriteCategory, PageRequest.of(0, SPOT_COUNT));
                return spots.stream().map(RecommendedSpotDto::new).collect(Collectors.toList());
            }
        }

        // 3. 여행 계획이 없는 일반 사용자
        // 3-a. 위치 정보가 있는 경우: 위치 기반 + 선호 카테고리 추천
        if (userLat != null && userLon != null) {
            List<TouristSpotWithDistance> projections = touristSpotRepository.findSpotsByCategoryAndLocation(
                    favoriteCategory,
                    userLat,
                    userLon,
                    CATEGORY_SUB_WEIGHT,
                    DISTANCE_WEIGHT_FOR_CAT,
                    SPOT_COUNT
            );
            return projections.stream().map(RecommendedSpotDto::new).collect(Collectors.toList());
        }
        // 3-b. 위치 정보가 없는 경우: 전국 단위 + 선호 카테고리 추천 (폴백)
        else {
            List<TouristSpot> spots = touristSpotRepository.findByCategoryOrderBySubWeightDesc(favoriteCategory, PageRequest.of(0, SPOT_COUNT));
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

    private String determineFavoriteCategory(User user) {
        return bookMarkRepository.findByBookMarkFolder_User(user).stream()
                .map(BookMark::getTouristSpot)
                .filter(spot -> spot != null && spot.getCategory() != null)
                .map(TouristSpot::getCategory)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }


}
