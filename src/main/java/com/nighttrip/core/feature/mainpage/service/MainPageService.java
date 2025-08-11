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
import com.nighttrip.core.feature.mainpage.dto.RecommendationResponseDto;
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


    public RecommendationResponseDto getNightPopularSpots(User user, Double userLat, Double userLon) {
        Pageable topSpots = PageRequest.of(0, SPOT_COUNT);

        Page<RecommendedSpotDto> spotsPage = getNightPopularSpotsPaginated(user, userLat, userLon, topSpots);

        List<RecommendedSpotDto> spots = new ArrayList<>(spotsPage.getContent());

        Collections.reverse(spots);

        boolean isMore = spotsPage.getTotalElements() > spotsPage.getNumberOfElements();

        return new RecommendationResponseDto(spots, isMore);
    }

    public CategoryRecommendationDto getCategoryRecommendedSpots(User user, Double userLat, Double userLon) {

        SpotCategory recommendedCategory = determineMainCategory(user);
        Pageable topTen = PageRequest.of(0, SPOT_COUNT);
        Page<RecommendedSpotDto> spotsPage = getSpotsByCategoryPaginated(user, userLat, userLon, recommendedCategory, topTen);
        List<RecommendedSpotDto> spotDtos = spotsPage.getContent();
        boolean isMore = spotsPage.getTotalElements() > spotsPage.getNumberOfElements();
        String nickname = (user != null) ? user.getNickname() : null;
        return new CategoryRecommendationDto(recommendedCategory, spotDtos, isMore, nickname);
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

    public Map<String, Object> getCategoryRecommendedSpotsPaginated(User user, Double userLat, Double userLon, String categoryName, Pageable pageable) {
        String nickname = (user != null) ? user.getNickname() : null;
        SpotCategory targetCategory = SpotCategory.fromValue(categoryName);
        Page<RecommendedSpotDto> spotsPage = getSpotsByCategoryPaginated(user, userLat, userLon, targetCategory, pageable);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("content", spotsPage.getContent());
        responseMap.put("pageable", spotsPage.getPageable());
        responseMap.put("totalPages", spotsPage.getTotalPages());
        responseMap.put("totalElements", spotsPage.getTotalElements());
        responseMap.put("last", spotsPage.isLast());
        responseMap.put("numberOfElements", spotsPage.getNumberOfElements());
        responseMap.put("first", spotsPage.isFirst());
        responseMap.put("size", spotsPage.getSize());
        responseMap.put("number", spotsPage.getNumber());
        responseMap.put("sort", spotsPage.getSort());
        responseMap.put("empty", spotsPage.isEmpty());

        responseMap.put("nickname", nickname);

        return responseMap;
    }


    // --- 헬퍼 메소드들 ---

    private Page<RecommendedSpotDto> getSpotsByCategoryPaginated(User user, Double userLat, Double userLon, SpotCategory category, Pageable pageable) {
        Optional<TripPlan> activePlanOpt = (user != null) ? tripPlanRepository.findFirstByUserAndStatusInOrderByStartDateAsc(user, List.of(TripStatus.UPCOMING, TripStatus.ONGOING)) : Optional.empty();
        if (activePlanOpt.isPresent()) {
            City targetCity = findTargetCityFromPlan(activePlanOpt.get());
            if (targetCity != null) {
                return touristSpotRepository.findByCityAndCategoryOrderBySubWeightDescIdAsc(targetCity, category, pageable).map(this::toRecommendedSpotDto);
            }
        }

        if (userLat != null && userLon != null) {
            long total = touristSpotRepository.countSpotsByCategoryAndLocation(category.name(), userLat, userLon);
            List<TouristSpotWithDistance> projections = touristSpotRepository.findSpotsByCategoryAndLocationPaginated(category.name(), userLat, userLon, CATEGORY_SUB_WEIGHT, DISTANCE_WEIGHT_FOR_CAT, pageable.getPageSize(), pageable.getOffset());
            return new PageImpl<>(projections.stream().map(this::toRecommendedSpotDto).collect(Collectors.toList()), pageable, total);
        } else {
            return touristSpotRepository.findByCategoryOrderBySubWeightDescIdAsc(category, pageable).map(this::toRecommendedSpotDto);
        }
    }

    private SpotCategory determineMainCategory(User user) {
        if (user == null) {
            List<SpotCategory> allCategories = touristSpotRepository.findAllDistinctCategories();
            if (allCategories.isEmpty()) {
                return SpotCategory.getRandomCategory();
            }
            return allCategories.get(new Random().nextInt(allCategories.size()));
        } else {
            return Optional.ofNullable(determineFavoriteCategory(user))
                    .orElseGet(() -> {
                        List<SpotCategory> allCategories = touristSpotRepository.findAllDistinctCategories();
                        if (allCategories.isEmpty()) {
                            return SpotCategory.getRandomCategory();
                        }
                        return allCategories.get(new Random().nextInt(allCategories.size()));
                    });
        }
    }
    // dev 브랜치의 이미지 조회 로직을 반영하기 위한 private 헬퍼 메소드
    private RecommendedSpotDto toRecommendedSpotDto(TouristSpot spot) {
        String imageUrl = imageRepository.findTHUMBNAILImage(String.valueOf(ImageType.TOURIST_SPOT), spot.getId())
                .map(ImageUrl::getUrl)
                .orElse(null);
        return new RecommendedSpotDto(spot, imageUrl);
    }

    // TouristSpotWithDistance를 위한 오버로딩
    private RecommendedSpotDto toRecommendedSpotDto(TouristSpotWithDistance projection) {
        String imageUrl = imageRepository.findTHUMBNAILImage(String.valueOf(ImageType.TOURIST_SPOT), projection.getId())
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
                new PartnerServiceDto(1L, "교통권", "https://kr.object.ncloudstorage.com/nighttrip-images-bucket/icon/airplane_icon.png"),
                new PartnerServiceDto(2L, "숙박예약", "https://kr.object.ncloudstorage.com/nighttrip-images-bucket/icon/hotel_icon.png"),
                new PartnerServiceDto(3L, "투어티켓", "https://kr.object.ncloudstorage.com/nighttrip-images-bucket/icon/ticket_icon.png"),
                new PartnerServiceDto(4L, "렌터카", "https://kr.object.ncloudstorage.com/nighttrip-images-bucket/icon/car_icon.png")
        );
    }

}
