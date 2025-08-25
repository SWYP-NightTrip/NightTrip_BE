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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

    public Page<CityResponseDto> getPopularCitiesAll(Pageable pageable) {
        Page<City> cityPage = cityRepository.findCitiesOrderByRecommendedScore(pageable);

        return cityPage.map(city -> {
            String imageUrl = imageRepository
                    .findSEARCHImage(String.valueOf(ImageType.CITY), city.getId())
                    .map(ImageUrl::getUrl)
                    .orElse(null);
            return CityResponseDto.from(city, imageUrl);
        });
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

        String monthlyKey = String.format("%s%d-%02d", POPULAR_CITIES_KEY_PREFIX, year, month);
        Set<String> trendingCityNames = redisTemplate.opsForZSet().reverseRange(monthlyKey, 0, TOTAL_SIZE * 2); // 중복될 수 있으니 넉넉하게 조회

        Set<City> finalCitiesSet = new LinkedHashSet<>();

        if (trendingCityNames != null && !trendingCityNames.isEmpty()) {
            trendingCityNames.forEach(name -> {
                if (finalCitiesSet.size() < TOTAL_SIZE) {
                    String normalizedKeyword = normalizeKeyword(name);
                    List<City> foundCities = cityRepository.findByCityNameWithLike(normalizedKeyword);
                    finalCitiesSet.addAll(foundCities);
                }
            });
        }

        if (finalCitiesSet.size() < TOTAL_SIZE) {
            Pageable pageable = PageRequest.of(0, 10);
            List<City> defaultCities = cityRepository.findAllByOrderByIdAsc(pageable);

            for (City defaultCity : defaultCities) {
                if (finalCitiesSet.size() >= TOTAL_SIZE) {
                    break;
                }
                finalCitiesSet.add(defaultCity);
            }
        }

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

    public Page<CityResponseDto> getMonthlyTrendingCitiesAll(int year, int month, Pageable pageable) {
        String monthlyKey = String.format("%s%d-%02d", POPULAR_CITIES_KEY_PREFIX, year, month);

        Set<String> allTrendingCityNames = redisTemplate.opsForZSet().reverseRange(monthlyKey, 0, -1);

        if (allTrendingCityNames == null || allTrendingCityNames.isEmpty()) {
            return getPopularCitiesAll(pageable);
        }

        Set<City> fullRecommendedCitySet = new LinkedHashSet<>();
        allTrendingCityNames.forEach(name -> {
            String normalizedKeyword = normalizeKeyword(name);
            List<City> foundCities = cityRepository.findByCityNameWithLike(normalizedKeyword);
            fullRecommendedCitySet.addAll(foundCities);
        });

        List<City> fullRecommendedList = new ArrayList<>(fullRecommendedCitySet);

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), fullRecommendedList.size());

        List<City> pagedList = (start > end) ? Collections.emptyList() : fullRecommendedList.subList(start, end);

        List<CityResponseDto> dtoList = pagedList.stream()
                .map(city -> {
                    String imageUrl = imageRepository
                            .findSEARCHImage(String.valueOf(ImageType.CITY), city.getId())
                            .map(ImageUrl::getUrl)
                            .orElse(null);
                    return CityResponseDto.from(city, imageUrl);
                })
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, fullRecommendedList.size());
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