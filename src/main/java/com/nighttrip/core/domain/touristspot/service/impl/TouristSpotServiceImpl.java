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
import com.nighttrip.core.global.enums.ImageType;
import com.nighttrip.core.global.exception.BusinessException;
import com.nighttrip.core.global.exception.CityNotFoundException;
import com.nighttrip.core.global.image.entity.ImageSizeType;
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
            return Collections.emptyList(); // 빈 검색어는 빈 리스트 반환
        }

        // 1. type이 'tourist_spot'인 문서만 필터링
        Query typeFilter = Query.of(q -> q.term(t -> t.field("type").value("tourist_spot")));

        // 2. 여러 필드에서 키워드 검색
        Query multiMatchQuery = Query.of(q -> q
                .multiMatch(m -> m
                        .fields("name", "suggestName", "description", "cityName", "category")
                        .query(keyword)
                        .fuzziness("AUTO")
                        .operator(Operator.And)
                )
        );

        // 3. 필터와 검색 쿼리 결합
        Query finalQuery = Query.of(q -> q.bool(b -> b.must(multiMatchQuery).filter(typeFilter)));

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(finalQuery)
                .withMaxResults(10)
                .build();

        // 4. Elasticsearch에서 ID 목록 검색
        SearchHits<SearchDocument> searchHits = elasticsearchOperations.search(nativeQuery, SearchDocument.class);
        List<Long> spotIds = searchHits.getSearchHits().stream()
                .map(hit -> Long.parseLong(hit.getContent().getId().replace("tourist_spot_", "")))
                .collect(Collectors.toList());

        if (spotIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 5. Redis에 검색 키워드 인기도 증가
        redisTemplate.opsForZSet().incrementScore(POPULAR_TOURIST_SPOTS_KEY, keyword.trim(), 1);

        // 6. DB에서 ID를 기반으로 전체 정보 조회 후, 기존 DTO 매핑 메소드 재활용
        List<TouristSpot> spots = touristSpotRepository.findAllById(spotIds);
        return spots.stream()
                .map(this::mapToTouristSpotResponseDto) // 기존 DTO 변환 메소드 재사용
                .collect(Collectors.toList());
    }
}