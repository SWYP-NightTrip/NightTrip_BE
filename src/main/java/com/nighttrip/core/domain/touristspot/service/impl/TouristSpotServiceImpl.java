package com.nighttrip.core.domain.touristspot.service.impl;

import com.nighttrip.core.domain.city.repository.CityRepository;
import com.nighttrip.core.domain.touristspot.dto.SpotDetailsDto;
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
import com.nighttrip.core.global.exception.BusinessException;
import com.nighttrip.core.global.exception.CityNotFoundException;
import com.nighttrip.core.global.image.entity.ImageSizeType;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TouristSpotServiceImpl implements TouristSpotService {

    private final TouristSpotRepository touristSpotRepository;
    private final TouristSpotLikeRepository touristSpotLIkeRepository;
    private final TouristSpotReviewRepository touristSpotReviewRepository;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final CityRepository cityRepository;


    private TouristSpotResponseDto mapToTouristSpotResponseDto(TouristSpot spot) {
        String imageUrl = imageRepository.findSEARCHImage(String.valueOf(ImageType.TOURIST_SPOT), spot.getId())
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

        List<String> images = imageRepository.findByImageTypeAndRelatedId(ImageType.TOURIST_SPOT, touristSpotId)
                .stream()
                .filter(image -> image.getImageSizeType() == ImageSizeType.DETAIL)
                .map(ImageUrl::getUrl)
                .collect(Collectors.toList());

        List<SpotDetailsDto> spotDetails = touristSpot.getTouristSpotDetails()
                .stream().map(d -> new SpotDetailsDto(d.getTypeKey(), d.getKoreanName()))
                .toList();

        boolean isLiked = false;

        Optional<String> userEmailOpt = SecurityUtils.findCurrentUserEmail();

        if (userEmailOpt.isPresent()) {
            Optional<User> currentUserOpt = userRepository.findByEmail(userEmailOpt.get());

            if (currentUserOpt.isPresent()) {
                User currentUser = currentUserOpt.get();
                isLiked = touristSpotLIkeRepository.existsByUserAndTouristSpot(currentUser, touristSpot);
            }
        }

        return TouristSpotDetailResponse.fromEntity(touristSpot, avg, countSum, isLiked, images, spotDetails);
    }

    @Override
    @Transactional
    public void addLike(Long touristSpotId) {
        TouristSpot touristSpot = touristSpotRepository.findById(touristSpotId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TOURIST_SPOT_NOT_FOUND));

        String userEmail = SecurityUtils.getCurrentUserEmail();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Optional<TourLike> existingLike = touristSpotLIkeRepository.findByUserAndTouristSpot(user, touristSpot);

        if (existingLike.isPresent()) {
            touristSpotLIkeRepository.delete(existingLike.get());
        } else {
            TourLike newLike = new TourLike(user, touristSpot);
            touristSpotLIkeRepository.save(newLike);
        }
    }
}