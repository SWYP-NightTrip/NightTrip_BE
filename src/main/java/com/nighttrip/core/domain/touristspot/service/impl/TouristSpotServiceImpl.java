package com.nighttrip.core.domain.touristspot.service.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
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
import com.nighttrip.core.global.dto.SearchDocument;
import com.nighttrip.core.global.enums.ErrorCode;
import com.nighttrip.core.global.enums.ImageSizeType;
import com.nighttrip.core.global.enums.ImageType;
import com.nighttrip.core.global.exception.BusinessException;
import com.nighttrip.core.global.exception.CityNotFoundException;
import com.nighttrip.core.global.image.entity.ImageUrl;
import com.nighttrip.core.global.image.repository.ImageRepository;
import com.nighttrip.core.global.oauth.util.SecurityUtils;
import com.nighttrip.core.global.util.LocationFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
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

    private final ElasticsearchOperations elasticsearchOperations;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String POPULAR_TOURIST_SPOTS_KEY = "popular:tourist_spots:keywords";

    private TouristSpotResponseDto mapToTouristSpotResponseDto(TouristSpot spot) {
        String imageUrl = imageRepository.findSEARCHImage(String.valueOf(ImageType.TOURIST_SPOT), spot.getId())
                .map(ImageUrl::getUrl)
                .orElse(null);

        return new TouristSpotResponseDto(
                spot.getId(),
                spot.getSpotName(),
                LocationFormatter.formatForSearch(spot.getAddress()),
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
    @Transactional(readOnly = true)
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
        List<String> hashTags = touristSpot.getHashTagsAsList();

        return TouristSpotDetailResponse.fromEntity(touristSpot, avg, countSum, isLiked, images, hashTags, spotDetails);
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

    @Override
    public List<TouristSpotResponseDto> searchTouristSpots(String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }

        Query typeFilter = Query.of(q -> q.term(t -> t.field("type").value("tourist_spot")));

        Query multiMatchQuery = Query.of(q -> q
                .multiMatch(m -> m
                        .fields(
                                "name^4",
                                "category^2",
                                "cityName^2",
                                "address^2",
                                "description",
                                "name.autocomplete"
                        )
                        .query(keyword)
                        .fuzziness("AUTO")
                        .operator(Operator.Or)
                        .minimumShouldMatch("70%")
                )
        );

        Query finalQuery = Query.of(q -> q.bool(b -> b.must(multiMatchQuery).filter(typeFilter)));

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(finalQuery)
                .withMaxResults(10)
                .build();

        SearchHits<SearchDocument> searchHits = elasticsearchOperations.search(nativeQuery, SearchDocument.class);
        List<Long> spotIds = searchHits.getSearchHits().stream()
                .map(hit -> Long.parseLong(hit.getContent().getId().replace("tourist_spot_", "")))
                .collect(Collectors.toList());

        if (spotIds.isEmpty()) {
            return Collections.emptyList();
        }

        redisTemplate.opsForZSet().incrementScore(POPULAR_TOURIST_SPOTS_KEY, keyword.trim(), 1);

        return searchHits.getSearchHits().stream()
                .map(hit -> {
                    SearchDocument doc = hit.getContent();

                    String formattedAddress = LocationFormatter.formatForSearch(doc.getAddress());

                    return new TouristSpotResponseDto(
                            Long.parseLong(doc.getId().replace("tourist_spot_", "")),
                            doc.getName(),
                            formattedAddress,
                            doc.getCategory(),
                            doc.getDescription(),
                            doc.getImageUrl()
                    );
                })
                .collect(Collectors.toList());
    }

    private static final int EARTH_RADIUS_KM = 6371;

    public double calculateDistanceBetweenSpots(Long spotOneId, Long spotTwoId) {
        TouristSpot spotOne = touristSpotRepository.findById(spotOneId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TOURIST_SPOT_NOT_FOUND));

        TouristSpot spotTwo = touristSpotRepository.findById(spotTwoId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TOURIST_SPOT_NOT_FOUND));

        if (spotOne.getLatitude() == null || spotOne.getLongitude() == null ||
                spotTwo.getLatitude() == null || spotTwo.getLongitude() == null) {
            throw new BusinessException(ErrorCode.INVALID_COORDINATE);
        }

        Double lat1Rad = Math.toRadians(spotOne.getLatitude());
        Double lon1Rad = Math.toRadians(spotOne.getLongitude());
        Double lat2Rad = Math.toRadians(spotTwo.getLatitude());
        Double lon2Rad = Math.toRadians(spotTwo.getLongitude());

        Double deltaLat = lat2Rad - lat1Rad;
        Double deltaLon = lon2Rad - lon1Rad;

        Double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }
}