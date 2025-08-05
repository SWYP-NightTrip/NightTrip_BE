package com.nighttrip.core.feature.mainpage.service;

import com.nighttrip.core.domain.city.entity.City;
import com.nighttrip.core.domain.touristspot.dto.TouristSpotWithDistance;
import com.nighttrip.core.domain.touristspot.entity.TouristSpot;
import com.nighttrip.core.domain.touristspot.repository.TouristSpotRepository;
import com.nighttrip.core.domain.tripday.entity.CityOnTripDay;
import com.nighttrip.core.domain.tripday.entity.TripDay;
import com.nighttrip.core.domain.tripplan.entity.TripPlan;
import com.nighttrip.core.domain.tripplan.repository.TripPlanRepository;
import com.nighttrip.core.domain.user.entity.BookMark;
import com.nighttrip.core.domain.user.entity.User;
import com.nighttrip.core.domain.user.repository.BookMarkRepository;
import com.nighttrip.core.feature.mainpage.dto.CategoryRecommendationDto;
import com.nighttrip.core.global.enums.*;
import com.nighttrip.core.feature.mainpage.dto.PartnerServiceDto;
import com.nighttrip.core.feature.mainpage.dto.RecommendedSpotDto;
import com.nighttrip.core.global.enums.SpotCategory;
import com.nighttrip.core.global.enums.TripStatus;
import com.nighttrip.core.global.image.entity.ImageUrl;
import com.nighttrip.core.global.image.repository.ImageRepository;
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
    private final ImageRepository imageRepository;

    private static final int SPOT_COUNT = 10;
    private static final double DISTANCE_WEIGHT = 0.50;
    private static final double MAIN_WEIGHT_FOR_DISTANCE = 0.35;
    private static final double REVIEW_WEIGHT_FOR_DISTANCE = 0.15;
    private static final double MAIN_WEIGHT_NO_DISTANCE = 0.70;
    private static final double REVIEW_WEIGHT_NO_DISTANCE = 0.30;
    private static final double CATEGORY_SUB_WEIGHT = 0.5;
    private static final double DISTANCE_WEIGHT_FOR_CAT = 0.5;


    public List<RecommendedSpotDto> getNightPopularSpots(User user, Double userLat, Double userLon) {
        Pageable topTen = PageRequest.of(0, SPOT_COUNT);
        // 페이지네이션 메소드를 호출하고, 내용물(content)만 반환하여 코드 중복 최소화
        return getNightPopularSpotsPaginated(user, userLat, userLon, topTen).getContent();
    }

    public CategoryRecommendationDto getCategoryRecommendedSpots(User user, Double userLat, Double userLon) {
        // 추천할 카테고리를 결정
        SpotCategory recommendedCategory = determineMainCategory(user);

        // 결정된 카테고리를 기반으로 TOP 10 여행지 목록을 가져옴
        Pageable topTen = PageRequest.of(0, SPOT_COUNT);
        Page<RecommendedSpotDto> spotsPage = getSpotsByCategoryPaginated(user, userLat, userLon, recommendedCategory, topTen);
        List<RecommendedSpotDto> spotDtos = spotsPage.getContent();

        // 새로운 DTO에 카테고리 정보와 여행지 목록을 담아 반환
        return new CategoryRecommendationDto(recommendedCategory, spotDtos);
    }


    // --- "더보기(페이지네이션)" 추천 로직 ---

    public Page<RecommendedSpotDto> getNightPopularSpotsPaginated(User user, Double userLat, Double userLon, Pageable pageable) {
        if (user == null) {
            if (userLat != null && userLon != null) {
                // 비로그인 + 위치 O
                long total = touristSpotRepository.countNearbyPopularSpots(userLat, userLon);
                List<TouristSpotWithDistance> projections = touristSpotRepository.findNearbyPopularSpotsPaginated(userLat, userLon, DISTANCE_WEIGHT, MAIN_WEIGHT_FOR_DISTANCE, REVIEW_WEIGHT_FOR_DISTANCE, pageable.getPageSize(), pageable.getOffset());
                return new PageImpl<>(projections.stream().map(this::toRecommendedSpotDto).collect(Collectors.toList()), pageable, total);
            } else {
                // 비로그인 + 위치 X
                Page<TouristSpot> spots = touristSpotRepository.findSpotsByScoresWithoutLocationPaginated(MAIN_WEIGHT_NO_DISTANCE, REVIEW_WEIGHT_NO_DISTANCE, pageable);
                return spots.map(this::toRecommendedSpotDto);
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
                    return new PageImpl<>(projections.stream().map(this::toRecommendedSpotDto).collect(Collectors.toList()), pageable, total);
                } else {
                    // 로그인 + 여행 계획 O + 위치 X
                    Page<TouristSpot> spots = touristSpotRepository.findSpotsInCityByScoresWithoutLocationPaginated(targetCity.getId(), MAIN_WEIGHT_NO_DISTANCE, REVIEW_WEIGHT_NO_DISTANCE, pageable);
                    return spots.map(this::toRecommendedSpotDto);
                }
            }
        }

        // 로그인 + 여행 계획 X
        if (userLat != null && userLon != null) {
            long total = touristSpotRepository.countNearbyPopularSpots(userLat, userLon);
            List<TouristSpotWithDistance> projections = touristSpotRepository.findNearbyPopularSpotsPaginated(userLat, userLon, DISTANCE_WEIGHT, MAIN_WEIGHT_FOR_DISTANCE, REVIEW_WEIGHT_FOR_DISTANCE, pageable.getPageSize(), pageable.getOffset());
            return new PageImpl<>(projections.stream().map(this::toRecommendedSpotDto).collect(Collectors.toList()), pageable, total);
        } else {
            // 로그인 + 여행 계획 X + 위치 X
            Page<TouristSpot> spots = touristSpotRepository.findSpotsByScoresWithoutLocationPaginated(MAIN_WEIGHT_NO_DISTANCE, REVIEW_WEIGHT_NO_DISTANCE, pageable);
            return spots.map(this::toRecommendedSpotDto);
        }
    }

    public Page<RecommendedSpotDto> getCategoryRecommendedSpotsPaginated(User user, Double userLat, Double userLon, String categoryName, Pageable pageable) {
        SpotCategory targetCategory = SpotCategory.fromValue(categoryName);
        return getSpotsByCategoryPaginated(user, userLat, userLon, targetCategory, pageable);
    }


    // --- 헬퍼 메소드들 ---

    private Page<RecommendedSpotDto> getSpotsByCategoryPaginated(User user, Double userLat, Double userLon, SpotCategory category, Pageable pageable) {
        Optional<TripPlan> activePlanOpt = (user != null) ? tripPlanRepository.findFirstByUserAndStatusInOrderByStartDateAsc(user, List.of(TripStatus.UPCOMING, TripStatus.ONGOING)) : Optional.empty();
        if (activePlanOpt.isPresent()) {
            City targetCity = findTargetCityFromPlan(activePlanOpt.get());
            if (targetCity != null) {
                return touristSpotRepository.findByCityAndCategoryOrderBySubWeightDesc(targetCity, category, pageable).map(this::toRecommendedSpotDto);
            }
        }

        if (userLat != null && userLon != null) {
            long total = touristSpotRepository.countSpotsByCategoryAndLocation(category.name(), userLat, userLon);
            List<TouristSpotWithDistance> projections = touristSpotRepository.findSpotsByCategoryAndLocationPaginated(category.name(), userLat, userLon, CATEGORY_SUB_WEIGHT, DISTANCE_WEIGHT_FOR_CAT, pageable.getPageSize(), pageable.getOffset());
            return new PageImpl<>(projections.stream().map(this::toRecommendedSpotDto).collect(Collectors.toList()), pageable, total);
        } else {
            return touristSpotRepository.findByCategoryOrderBySubWeightDesc(category, pageable).map(this::toRecommendedSpotDto);
        }
    }

    private SpotCategory determineMainCategory(User user) {
        if (user == null) {
            List<SpotCategory> allCategories = touristSpotRepository.findAllDistinctCategories();
            return allCategories.get(new Random().nextInt(allCategories.size()));
        } else {
            return Optional.ofNullable(determineFavoriteCategory(user))
                    .orElseGet(() -> {
                        List<SpotCategory> allCategories = touristSpotRepository.findAllDistinctCategories();
                        return allCategories.get(new Random().nextInt(allCategories.size()));
                    });
        }
    }

    // dev 브랜치의 이미지 조회 로직을 반영하기 위한 private 헬퍼 메소드
    private RecommendedSpotDto toRecommendedSpotDto(TouristSpot spot) {
        String imageUrl = imageRepository.findMainImageByTypeAndRelatedId(ImageType.TOURIST_SPOT, spot.getId())
                .map(ImageUrl::getUrl)
                .orElse(null);
        return new RecommendedSpotDto(spot, imageUrl);
    }

    // TouristSpotWithDistance를 위한 오버로딩
    private RecommendedSpotDto toRecommendedSpotDto(TouristSpotWithDistance projection) {
        String imageUrl = imageRepository.findMainImageByTypeAndRelatedId(ImageType.TOURIST_SPOT, projection.getId())
                .map(ImageUrl::getUrl)
                .orElse(null);
        return new RecommendedSpotDto(projection, imageUrl);
    }

    private City findTargetCityFromPlan(TripPlan activePlan) {
        LocalDate today = LocalDate.now();
        Optional<TripDay> todayTripDayOpt = activePlan.getTripDays().stream().filter(day -> activePlan.getStartDate().plusDays(day.getDayOrder() - 1).equals(today)).findFirst();
        Optional<City> todayCityOpt = todayTripDayOpt.flatMap(day -> day.getCityOnTripDays().stream().findFirst().map(CityOnTripDay::getCity));
        return todayCityOpt.or(() -> activePlan.getTripDays().stream().filter(day -> !activePlan.getStartDate().plusDays(day.getDayOrder() - 1).isBefore(today)).sorted(Comparator.comparing(TripDay::getDayOrder)).flatMap(day -> day.getCityOnTripDays().stream()).map(CityOnTripDay::getCity).filter(Objects::nonNull).findFirst()).orElse(null);
    }

    private SpotCategory determineFavoriteCategory(User user) {
        return bookMarkRepository.findByBookMarkFolder_User(user).stream()
                .map(BookMark::getTouristSpot)
                .filter(spot -> spot != null && spot.getCategory() != null)
                .collect(Collectors.groupingBy(TouristSpot::getCategory, Collectors.counting()))
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
