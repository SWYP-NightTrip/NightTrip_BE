package com.nighttrip.core.feature.mainpage.service;

import com.nighttrip.core.domain.city.entity.City;
import com.nighttrip.core.domain.touristspot.dto.TouristSpotWithDistance;
import com.nighttrip.core.domain.touristspot.entity.TouristSpot;
import com.nighttrip.core.domain.touristspot.entity.TouristSpotReview;
import com.nighttrip.core.domain.touristspot.repository.TouristSpotRepository;
import com.nighttrip.core.domain.tripday.entity.CityOnTripDay;
import com.nighttrip.core.domain.tripday.entity.TripDay;
import com.nighttrip.core.domain.tripplan.entity.TripPlan;
import com.nighttrip.core.domain.tripplan.repository.TripPlanRepository;
import com.nighttrip.core.domain.user.entity.BookMark;
import com.nighttrip.core.domain.user.entity.User;
import com.nighttrip.core.domain.user.repository.BookMarkRepository;
import com.nighttrip.core.feature.mainpage.dto.CategoryRecommendationDto;
import com.nighttrip.core.global.enums.SpotCategory;
import com.nighttrip.core.global.enums.TripStatus;
import com.nighttrip.core.feature.mainpage.dto.PartnerServiceDto;
import com.nighttrip.core.feature.mainpage.dto.RecommendedSpotDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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


    public CategoryRecommendationDto getCategoryRecommendedSpots(User user, Double userLat, Double userLon) {

        SpotCategory recommendedCategory;

        if (user == null) {
            List<SpotCategory> allCategories = touristSpotRepository.findAllDistinctCategories();
            if (allCategories.isEmpty()) {
                return new CategoryRecommendationDto(null, Collections.emptyList());
            }
            Random random = new Random();
            recommendedCategory = allCategories.get(random.nextInt(allCategories.size()));

        } else { // user가 null이 아닐 때
            // 1. 사용자의 북마크 기록을 바탕으로 가장 선호하는 카테고리를 찾습니다.
            SpotCategory favoriteCategory = determineFavoriteCategory(user);
            if (favoriteCategory == null) {
                List<SpotCategory> allCategories = touristSpotRepository.findAllDistinctCategories();
                if (allCategories.isEmpty()) {
                    return new CategoryRecommendationDto(null, Collections.emptyList());
                }
                Random random = new Random();
                recommendedCategory = allCategories.get(random.nextInt(allCategories.size()));
            } else {
                recommendedCategory = favoriteCategory;
            }
        }


        // (user가 null이면 activePlanOpt는 항상 비어있습니다)
        Optional<TripPlan> activePlanOpt = (user != null) ? tripPlanRepository.findFirstByUserAndStatusInOrderByStartDateAsc(user, List.of(TripStatus.UPCOMING, TripStatus.ONGOING)) : Optional.empty();

        if (activePlanOpt.isPresent()) {
            City targetCity = findTargetCityFromPlan(activePlanOpt.get());
            if (targetCity != null) {
                List<TouristSpot> spots = touristSpotRepository.findByCityAndCategoryOrderBySubWeightDesc(targetCity, recommendedCategory, PageRequest.of(0, SPOT_COUNT)).getContent();
                List<RecommendedSpotDto> spotDtos = spots.stream().map(RecommendedSpotDto::new).collect(Collectors.toList());
                return new CategoryRecommendationDto(recommendedCategory, spotDtos);
            }
        }

        // 3. 여행 계획이 없는 일반 사용자 (또는 비로그인 사용자)
        // 3-a. 위치 정보가 있는 경우: 위치 기반 + 선호 카테고리 추천
        if (userLat != null && userLon != null) {
            List<TouristSpotWithDistance> projections = touristSpotRepository.findSpotsByCategoryAndLocation(
                    recommendedCategory,
                    userLat,
                    userLon,
                    CATEGORY_SUB_WEIGHT,
                    DISTANCE_WEIGHT_FOR_CAT,
                    SPOT_COUNT
            );
            List<RecommendedSpotDto> spotDtos = projections.stream().map(RecommendedSpotDto::new).collect(Collectors.toList());
            return new CategoryRecommendationDto(recommendedCategory, spotDtos);
        }
        // 3-b. 위치 정보가 없는 경우: 전국 단위 + 선호 카테고리 추천 (폴백)
        else {
            List<TouristSpot> spots = touristSpotRepository.findByCategoryOrderBySubWeightDesc(recommendedCategory, PageRequest.of(0, SPOT_COUNT)).getContent();
            List<RecommendedSpotDto> spotDtos = spots.stream().map(RecommendedSpotDto::new).collect(Collectors.toList());
            return new CategoryRecommendationDto(recommendedCategory, spotDtos);
        }
    }

    // --- "더보기(페이지네이션)" 추천 로직 ---

    public Page<RecommendedSpotDto> getNightPopularSpotsPaginated(User user, Double userLat, Double userLon, Pageable pageable) {
        // [수정] 이 메소드 전체가 수정 대상입니다.
        if (user == null) {
            if (userLat != null && userLon != null) {
                // 비로그인 + 위치 O
                long total = touristSpotRepository.countNearbyPopularSpots(userLat, userLon);
                List<TouristSpotWithDistance> projections = touristSpotRepository.findNearbyPopularSpotsPaginated(userLat, userLon, DISTANCE_WEIGHT, MAIN_WEIGHT_FOR_DISTANCE, REVIEW_WEIGHT_FOR_DISTANCE, pageable.getPageSize(), pageable.getOffset());
                return new PageImpl<>(projections.stream().map(RecommendedSpotDto::new).collect(Collectors.toList()), pageable, total);
            } else {
                // 비로그인 + 위치 X
                Page<TouristSpot> spots = touristSpotRepository.findSpotsByScoresWithoutLocationPaginated(MAIN_WEIGHT_NO_DISTANCE, REVIEW_WEIGHT_NO_DISTANCE, pageable);
                return spots.map(RecommendedSpotDto::new);
            }
        }

        Optional<TripPlan> activePlanOpt = tripPlanRepository.findFirstByUserAndStatusInOrderByStartDateAsc(user, List.of(TripStatus.UPCOMING, TripStatus.ONGOING));
        if (activePlanOpt.isPresent()) {
            City targetCity = findTargetCityFromPlan(activePlanOpt.get());
            if (targetCity != null) {
                if (userLat != null && userLon != null) {
                    // 로그인 + 여행 계획 O + 위치 O
                    long total = touristSpotRepository.countSpotsInCityWithScores(targetCity.getId(), userLat, userLon);
                    List<TouristSpotWithDistance> projections = touristSpotRepository.findSpotsInCityWithScoresPaginated(targetCity.getId(), userLat, userLon, DISTANCE_WEIGHT, MAIN_WEIGHT_FOR_DISTANCE, REVIEW_WEIGHT_FOR_DISTANCE, pageable.getPageSize(), pageable.getOffset());
                    return new PageImpl<>(projections.stream().map(RecommendedSpotDto::new).collect(Collectors.toList()), pageable, total);
                } else {
                    // 로그인 + 여행 계획 O + 위치 X
                    Page<TouristSpot> spots = touristSpotRepository.findSpotsInCityByScoresWithoutLocationPaginated(targetCity.getId(), MAIN_WEIGHT_NO_DISTANCE, REVIEW_WEIGHT_NO_DISTANCE, pageable);
                    return spots.map(RecommendedSpotDto::new);
                }
            }
        }

        // 로그인 + 여행 계획 X
        if (userLat != null && userLon != null) {
            long total = touristSpotRepository.countNearbyPopularSpots(userLat, userLon);
            List<TouristSpotWithDistance> projections = touristSpotRepository.findNearbyPopularSpotsPaginated(userLat, userLon, DISTANCE_WEIGHT, MAIN_WEIGHT_FOR_DISTANCE, REVIEW_WEIGHT_FOR_DISTANCE, pageable.getPageSize(), pageable.getOffset());
            return new PageImpl<>(projections.stream().map(RecommendedSpotDto::new).collect(Collectors.toList()), pageable, total);
        } else {
            // 로그인 + 여행 계획 X + 위치 X
            Page<TouristSpot> spots = touristSpotRepository.findSpotsByScoresWithoutLocationPaginated(MAIN_WEIGHT_NO_DISTANCE, REVIEW_WEIGHT_NO_DISTANCE, pageable);
            return spots.map(RecommendedSpotDto::new);
        }
    }

    public Page<RecommendedSpotDto> getCategoryRecommendedSpotsPaginated(User user, Double userLat, Double userLon, String categoryName, Pageable pageable) {

        SpotCategory targetCategory = SpotCategory.fromValue(categoryName);

        Optional<TripPlan> activePlanOpt = (user != null) ? tripPlanRepository.findFirstByUserAndStatusInOrderByStartDateAsc(user, List.of(TripStatus.UPCOMING, TripStatus.ONGOING)) : Optional.empty();
        if (activePlanOpt.isPresent()) {
            City targetCity = findTargetCityFromPlan(activePlanOpt.get());
            if (targetCity != null) {
                // 여행 계획 O
                return touristSpotRepository.findByCityAndCategoryOrderBySubWeightDesc(targetCity, targetCategory, pageable).map(RecommendedSpotDto::new);
            }
        }

        if (userLat != null && userLon != null) {
            // 여행 계획 X + 위치 O
            long total = touristSpotRepository.countSpotsByCategoryAndLocation(targetCategory.name(), userLat, userLon);
            List<TouristSpotWithDistance> projections = touristSpotRepository.findSpotsByCategoryAndLocationPaginated(targetCategory.name(), userLat, userLon, CATEGORY_SUB_WEIGHT, DISTANCE_WEIGHT_FOR_CAT, pageable.getPageSize(), pageable.getOffset());
            return new PageImpl<>(projections.stream().map(RecommendedSpotDto::new).collect(Collectors.toList()), pageable, total);
        } else {
            // 여행 계획 X + 위치 X
            return touristSpotRepository.findByCategoryOrderBySubWeightDesc(targetCategory, pageable).map(RecommendedSpotDto::new);
        }
    }



    private City findTargetCityFromPlan(TripPlan activePlan) {
        LocalDate today = LocalDate.now();

        Optional<TripDay> todayTripDayOpt = activePlan.getTripDays().stream()
                .filter(day -> {
                    LocalDate tripDate = activePlan.getStartDate().plusDays(day.getDayOrder() - 1);
                    return tripDate.equals(today);
                })
                .findFirst();

        Optional<City> todayCityOpt = todayTripDayOpt.flatMap(day -> day.getCityOnTripDays().stream()
                .findFirst()
                .map(CityOnTripDay::getCity)
        );

        return todayCityOpt.or(() ->
                activePlan.getTripDays().stream()
                        .filter(day -> !activePlan.getStartDate().plusDays(day.getDayOrder() - 1).isBefore(today))
                        .sorted(Comparator.comparing(TripDay::getDayOrder))
                        .flatMap(day -> day.getCityOnTripDays().stream())
                        .map(CityOnTripDay::getCity)
                        .filter(Objects::nonNull)
                        .findFirst()
        ).orElse(null);
    }

    private RecommendedSpotDto convertToDtoWithoutDistance(TouristSpot spot) {
        return new RecommendedSpotDto(new TouristSpotWithDistance() {
            @Override public Long getId() { return spot.getId(); }
            @Override public String getSpotName() { return spot.getSpotName(); }
            @Override public String getCategory() { return spot.getCategory().getKoreanName(); }
            @Override public String getAddress() { return spot.getAddress(); }
            @Override public City getCity() { return spot.getCity(); }
            @Override public List<TouristSpotReview> getTouristSpotReviews() { return spot.getTouristSpotReviews(); }
            @Override public Double getDistance() { return null; }
        });
    }

    private SpotCategory determineFavoriteCategory(User user) {
        return bookMarkRepository.findByBookMarkFolder_User(user).stream()
                .map(BookMark::getTouristSpot)
                .filter(spot -> spot != null && spot.getCategory() != null)
                .map(spot -> {
                    try {
                        return SpotCategory.fromValue(spot.getCategory().getKoreanName());
                    } catch (Exception e) {
                        return null; // 변환 실패 시 null
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public List<PartnerServiceDto> getPartnerServices() {
        return Arrays.asList(
                new PartnerServiceDto(1L, "교통권", null),
                new PartnerServiceDto(2L, "숙박예약", null),
                new PartnerServiceDto(3L, "투어티켓", null),
                new PartnerServiceDto(4L, "렌터카", null)
        );
    }

}
