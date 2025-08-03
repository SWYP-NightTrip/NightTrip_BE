package com.nighttrip.core.domain.touristspot.service.impl;

import com.nighttrip.core.domain.city.repository.CityRepository;
import com.nighttrip.core.domain.touristspot.dto.TouristSpotDetailResponse;
import com.nighttrip.core.domain.touristspot.dto.TouristSpotResponseDto;
import com.nighttrip.core.domain.touristspot.entity.TourLike;
import com.nighttrip.core.domain.touristspot.entity.TouristSpot;
import com.nighttrip.core.domain.touristspot.entity.TouristSpotReview;
import com.nighttrip.core.domain.touristspot.repository.TouristSpotLikeRepository;
import com.nighttrip.core.domain.touristspot.repository.TouristSpotRepository;
import com.nighttrip.core.domain.touristspot.repository.TouristSpotReviewRepository;
import com.nighttrip.core.domain.touristspot.service.TouristSpotService;
import com.nighttrip.core.domain.user.entity.User;
import com.nighttrip.core.domain.user.repository.UserRepository;
import com.nighttrip.core.global.enums.ErrorCode;
import com.nighttrip.core.global.enums.ImageType;
import com.nighttrip.core.global.enums.SpotDetails;
import com.nighttrip.core.global.exception.BusinessException;
import com.nighttrip.core.global.exception.CityNotFoundException;
import com.nighttrip.core.global.image.entity.ImageUrl;
import com.nighttrip.core.global.image.repository.ImageRepository;
import com.nighttrip.core.global.oauth.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TouristSpotServiceImpl implements TouristSpotService {

    private final TouristSpotRepository touristSpotRepository;
    private final TouristSpotLikeRepository touristSpotLIkeRepository;
    private final TouristSpotReviewRepository touristSpotReviewRepository;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final CityRepository cityRepository;


    private TouristSpotResponseDto mapToTouristSpotResponseDto(TouristSpot spot) {
        String imageUrl = imageRepository.findMainImageByTypeAndRelatedId(ImageType.TOURIST_SPOT, spot.getId())
                .map(ImageUrl::getUrl)
                .orElse(null);

        return new TouristSpotResponseDto(
                spot.getId(),
                spot.getSpotName(),
                spot.getAddress(),
                spot.getCategory().getKoreanName(),
                spot.getSpotDescription(),
                imageUrl
        );
    }

    @Override
    public List<TouristSpotResponseDto> getPopularTouristSpotsInCity(Long cityId) {
        cityRepository.findById(cityId)
                .orElseThrow(CityNotFoundException::new);

        Pageable pageable = PageRequest.of(0, 7);
        List<TouristSpot> popularSpots = touristSpotRepository.findPopularTouristSpotsByCityId(cityId, pageable);

        return popularSpots.stream()
                .map(this::mapToTouristSpotResponseDto)
                .collect(Collectors.toList());
    }


    @Override
    public List<TouristSpotResponseDto> getRecommendedTouristSpotsInCity(Long cityId) {
        cityRepository.findById(cityId)
                .orElseThrow(CityNotFoundException::new);

        Pageable pageable = PageRequest.of(0, 7);
        List<TouristSpot> recommendedSpots = touristSpotRepository.findRecommendedTouristSpotsByCityId(cityId, pageable);

        return recommendedSpots.stream()
                .map(this::mapToTouristSpotResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public TouristSpotDetailResponse getTouristSpotDetail(Long touristSpotId) {
        TouristSpot touristSpot = touristSpotRepository.findById(touristSpotId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TOURIST_SPOT_NOT_FOUND));

        List<TouristSpotReview> touristSpotReviews = touristSpotReviewRepository.findByTouristSpotId(touristSpotId);
        DoubleSummaryStatistics reviewStatistics = touristSpotReviews.stream()
                .collect(Collectors.summarizingDouble(TouristSpotReview::getScope));

        Double avg = reviewStatistics.getAverage();
        Long countSum = reviewStatistics.getCount();

        String mainImage = imageRepository.findMainImageByTypeAndRelatedId(ImageType.TOURIST_SPOT, touristSpotId)
                .map(ImageUrl::getUrl)
                .orElse(null);

        List<String> images = imageRepository.findByImageTypeAndRelatedId(ImageType.TOURIST_SPOT, touristSpotId)
                .stream()
                .filter(image -> !image.isMain())
                .map(ImageUrl::getUrl)
                .collect(Collectors.toList());

        List<String> spotDetails = touristSpot.getTouristSpotDetails()
                .stream()
                .map(SpotDetails::getKoreanName)
                .toList();

        Boolean isLiked = touristSpotLIkeRepository.existsByTouristSpotId(touristSpotId);

        return TouristSpotDetailResponse.fromEntity(touristSpot, avg, countSum, mainImage, isLiked, images, spotDetails);
    }

    @Override
    public void addLike(Long touristSpotId) {
        TouristSpot touristSpot = touristSpotRepository.findById(touristSpotId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TOURIST_SPOT_NOT_FOUND));

        String userEmail = SecurityUtils.getCurrentUserEmail();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        TourLike tourLike = new TourLike(user, touristSpot);
        touristSpotLIkeRepository.save(tourLike);
    }
}