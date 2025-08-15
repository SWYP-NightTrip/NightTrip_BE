package com.nighttrip.core.domain.city.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import com.nighttrip.core.domain.city.Implementation.CitySearchServiceImpl;
import com.nighttrip.core.domain.city.dto.CityPopularityDto;
import com.nighttrip.core.domain.city.dto.CityResponseDto;
import com.nighttrip.core.domain.city.entity.City;
import com.nighttrip.core.domain.city.repository.CityRepository;
import com.nighttrip.core.global.dto.SearchDocument;
import com.nighttrip.core.global.enums.ErrorCode;
import com.nighttrip.core.global.enums.ImageType;
import com.nighttrip.core.global.exception.BusinessException;
import com.nighttrip.core.global.image.entity.ImageSizeType;
import com.nighttrip.core.global.image.entity.ImageUrl;
import com.nighttrip.core.global.image.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CitySearchService implements CitySearchServiceImpl {

    private final ElasticsearchOperations elasticsearchOperations;
    private final RedisTemplate<String, String> redisTemplate;
    private final CityRepository cityRepository;
    private final ImageRepository imageRepository;

    private static final String POPULAR_CITIES_KEY_PREFIX = "popular:cities:";

    @Override
    public List<CityResponseDto> searchCity(String keyword) {
        
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }

        Query typeFilter = QueryBuilders.term(t -> t.field("type").value("city"));
        Query multiMatchQuery = QueryBuilders.multiMatch(m -> m
                .fields("name", "suggestName")
                .query(keyword)
                .fuzziness("AUTO")
        );

        Query finalQuery = QueryBuilders.bool(b -> b.must(multiMatchQuery).filter(typeFilter));
        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(finalQuery)
                .withMaxResults(10)
                .build();

        SearchHits<SearchDocument> searchHits = elasticsearchOperations.search(nativeQuery, SearchDocument.class);

        String currentMonthKey = POPULAR_CITIES_KEY_PREFIX + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        redisTemplate.opsForZSet().incrementScore(currentMonthKey, keyword.trim(), 1);

        return searchHits.getSearchHits().stream()
                .map(hit -> CityResponseDto.from(hit.getContent()))
                .collect(Collectors.toList());
    }

    public List<CityResponseDto> getRecommendedCities() {
        List<City> cities = cityRepository.findCitiesOrderByRecommendedScore();

        return cities.stream()
                .limit(5)
                .map(city -> {
                    String imageUrl = imageRepository
                            .findSEARCHImage(String.valueOf(ImageType.CITY), city.getId())
                            .map(ImageUrl::getUrl)
                            .orElse(null);

                    return CityResponseDto.from(city, imageUrl);
                })
                .collect(Collectors.toList());
    }

    public List<CityResponseDto> getPopularCities() {
        Pageable topSeven = PageRequest.of(0, 7);

        List<CityPopularityDto> popularCitiesDto = cityRepository.findPopularCitiesWithAggregatedScores(topSeven);

        return popularCitiesDto.stream()
                .map(dto -> {
                    String imageUrl = imageRepository
                            .findSEARCHImage(String.valueOf(ImageType.CITY), dto.id())
                            .map(ImageUrl::getUrl)
                            .orElse(null);

                    return new CityResponseDto(dto.id(), dto.cityName(), imageUrl);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<CityResponseDto> getDefaultCities() {
        Pageable pageable = PageRequest.of(0, 7);
        List<City> defaultCities = cityRepository.findAllByOrderByIdAsc(pageable);

        return defaultCities.stream()
                .map(city -> {
                    String imageUrl = imageRepository
                            .findSEARCHImage(String.valueOf(ImageType.CITY), city.getId())
                            .map(ImageUrl::getUrl)
                            .orElse(null);

                    return CityResponseDto.from(city, imageUrl);
                })
                .collect(Collectors.toList());
    }

    public List<CityResponseDto> getMonthlyTrendingCities(int year, int month) {

        final int TOTAL_SIZE = 5;

        // 1. Redis에서 인기 도시 이름 목록을 가져옵니다. (순서 보장됨)
        String monthlyKey = String.format("%s%d-%02d", POPULAR_CITIES_KEY_PREFIX, year, month);
        Set<String> trendingCityNames = redisTemplate.opsForZSet().reverseRange(monthlyKey, 0, TOTAL_SIZE * 2); // 중복될 수 있으니 넉넉하게 조회

        // 2. 순서 유지 및 중복 방지를 위해 LinkedHashSet을 사용합니다.
        Set<City> finalCitiesSet = new LinkedHashSet<>();

        // 3. 인기 검색어 순서대로 LIKE 검색을 수행하고 결과를 Set에 추가합니다.
        if (trendingCityNames != null && !trendingCityNames.isEmpty()) {
            trendingCityNames.forEach(name -> {
                // 아직 5개를 다 못채웠을 때만 DB에 쿼리를 날립니다.
                if (finalCitiesSet.size() < TOTAL_SIZE) {
                    String normalizedKeyword = normalizeKeyword(name);
                    List<City> foundCities = cityRepository.findByCityNameWithLike(normalizedKeyword);
                    // LinkedHashSet에 추가하면 중복된 도시는 자동으로 걸러집니다.
                    finalCitiesSet.addAll(foundCities);
                }
            });
        }

        // 4. 인기 도시만으로 5개가 안 채워졌으면, 기본 도시로 나머지를 채웁니다.
        if (finalCitiesSet.size() < TOTAL_SIZE) {
            Pageable pageable = PageRequest.of(0, 10); // 넉넉하게 10개 조회
            List<City> defaultCities = cityRepository.findAllByOrderByIdAsc(pageable);

            for (City defaultCity : defaultCities) {
                if (finalCitiesSet.size() >= TOTAL_SIZE) {
                    break;
                }
                // Set.add는 이미 원소가 있으면 false를 반환하고 추가하지 않습니다.
                finalCitiesSet.add(defaultCity);
            }
        }

        // 5. 최종 Set을 DTO 리스트로 변환하여 반환합니다. 정확히 5개만 반환하도록 limit()을 사용합니다.
        return finalCitiesSet.stream()
                .limit(TOTAL_SIZE)
                .map(city -> {
                    String imageUrl = imageRepository
                            .findSEARCHImage(String.valueOf(ImageType.CITY), city.getId())
                            .map(ImageUrl::getUrl)
                            .orElse(null);
                    return CityResponseDto.from(city, imageUrl);
                })
                .collect(Collectors.toList());
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }

        switch (keyword) {
            case "서울시":
            case "서울":
                return "서울";
            case "부산시":
            case "부산":
                return "부산";
            case "대구시":
            case "대구":
                return "대구";
            case "세종시":
            case "세종":
                return "세종";

            case "충청도":
                return "충청";
            case "전라도":
                return "전라";
            case "경상도":
                return "경상";

            case "충북":
                return "충청북도";
            case "충남":
                return "충청남도";
            case "전북":
                return "전북특별자치도";
            case "전남":
                return "전라남도";
            case "경북":
                return "경상북도";
            case "경남":
                return "경상남도";
            case "제주도":
                return "제주특별자치도";

            case "강원도":
                return "강원특별자치도";

            default:
                return keyword;
        }
    }
}