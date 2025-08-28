package com.nighttrip.core.domain.tripplan.service;

import com.nighttrip.core.domain.city.entity.City;
import com.nighttrip.core.domain.city.repository.CityRepository;
import com.nighttrip.core.domain.touristspot.entity.TouristSpot;
import com.nighttrip.core.domain.touristspot.repository.TouristSpotRepository;
import com.nighttrip.core.domain.tripday.entity.CityOnTripDay;
import com.nighttrip.core.domain.tripplan.dto.TripPlanResponse;
import com.nighttrip.core.domain.tripplan.entity.TripPlan;
import com.nighttrip.core.domain.tripplan.repository.TripPlanRepository;
import com.nighttrip.core.domain.user.entity.User;
import com.nighttrip.core.domain.user.repository.UserRepository;
import com.nighttrip.core.feature.mainpage.dto.CategoryRecommendationDto;
import com.nighttrip.core.feature.mainpage.dto.RecommendedSpotDto;
import com.nighttrip.core.global.enums.ErrorCode;
import com.nighttrip.core.global.enums.ImageType;
import com.nighttrip.core.global.enums.SpotCategory;
import com.nighttrip.core.global.enums.TripStatus;
import com.nighttrip.core.global.exception.BusinessException;
import com.nighttrip.core.global.image.entity.ImageUrl;
import com.nighttrip.core.global.image.repository.ImageRepository;
import com.nighttrip.core.global.oauth.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpotRecommendationService {

    private final TouristSpotRepository touristSpotRepository;
    private final CityRepository cityRepository;
    private final ImageRepository imageRepository;
    private final TripPlanRepository tripPlanRepository;
    private static final List<SpotCategory> TOURISM_CATEGORIES = List.of(
            SpotCategory.NATURE,
            SpotCategory.CULTURE,
            SpotCategory.HISTORY,
            SpotCategory.ETC,
            SpotCategory.EXPERIENCE
    );

    public CategoryRecommendationDto getSpotsByCategoryPaginated(Long tripPlanId, String categoryName, Pageable pageable) {
        TripPlan tripPlan = tripPlanRepository.findById(tripPlanId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRIP_PLAN_NOT_FOUND));

        List<City> targetCities = tripPlan.getCityOnTripDays().stream()
                .map(CityOnTripDay::getCity)
                .distinct()
                .collect(Collectors.toList());

        Page<TouristSpot> spotsPage;
        SpotCategory categoryEnum;

        if ("관광".equals(categoryName)) {
            categoryEnum = SpotCategory.CULTURE;
            if (targetCities.isEmpty()) {
                spotsPage = Page.empty();
            } else {
                spotsPage = touristSpotRepository.findByCityInAndCategoryInOrderBySubWeightDescIdAsc(targetCities, TOURISM_CATEGORIES, pageable);
            }
        } else {
            categoryEnum = Arrays.stream(SpotCategory.values())
                    .filter(c -> c.getKoreanName().equals(categoryName)) // Enum의 한글 이름과 categoryName을 비교
                    .findFirst() // 일치하는 첫 번째 Enum을 찾음
                    .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_PLACE_CATEGORY)); // 없으면 예외 발생

            if (targetCities.isEmpty()) {
                spotsPage = Page.empty();
            } else {
                spotsPage = touristSpotRepository.findByCityInAndCategoryOrderBySubWeightDescIdAsc(targetCities, categoryEnum, pageable);
            }
        }


        List<RecommendedSpotDto> spotDtos = spotsPage.getContent().stream()
                .map(this::toRecommendedSpotDto)
                .collect(Collectors.toList());

        boolean isMore = spotsPage.hasNext();

        return new CategoryRecommendationDto(categoryEnum, spotDtos, isMore, null);
    }

    private RecommendedSpotDto toRecommendedSpotDto(TouristSpot spot) {
        String imageUrl = imageRepository.findTHUMBNAILImage(String.valueOf(ImageType.TOURIST_SPOT), spot.getId())
                .map(ImageUrl::getUrl)
                .orElse(null);
        return new RecommendedSpotDto(spot, imageUrl);
    }

}
