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

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CitySearchService implements CitySearchServiceImpl {

    private final ElasticsearchOperations elasticsearchOperations;
    private final RedisTemplate<String, String> redisTemplate;
    private final CityRepository cityRepository;
    private final ImageRepository imageRepository;

    private static final String POPULAR_CITIES_KEY = "popular:cities:keywords";

    @Override
    public List<CityResponseDto> searchCity(String keyword) {
        
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.EMPTY_SEARCH_KEYWORD);
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

        redisTemplate.opsForZSet().incrementScore(POPULAR_CITIES_KEY, keyword.trim(), 1);

        return searchHits.getSearchHits().stream()
                .map(hit -> CityResponseDto.from(hit.getContent()))
                .collect(Collectors.toList());
    }

    public List<CityResponseDto> getRecommendedCities() {
        List<City> cities = cityRepository.findCitiesOrderByRecommendedScore();

        return cities.stream()
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

}