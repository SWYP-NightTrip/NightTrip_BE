package com.nighttrip.core.domain.city.service;

import com.nighttrip.core.domain.city.Implementation.CitySearchServiceImpl;
import com.nighttrip.core.domain.city.dto.CityPopularityDto;
import com.nighttrip.core.domain.city.dto.CityResponseDto;
import com.nighttrip.core.domain.city.entity.City;
import com.nighttrip.core.domain.city.repository.CityRepository;
import com.nighttrip.core.global.enums.ImageType;
import com.nighttrip.core.global.image.entity.ImageSizeType;
import com.nighttrip.core.global.image.entity.ImageUrl;
import com.nighttrip.core.global.image.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CitySearchService implements CitySearchServiceImpl {

    private final CityRepository cityRepository;
    private final ImageRepository imageRepository;

    @Override
    public List<CityResponseDto> searchCity(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("검색어는 비어 있을 수 없습니다.");
        }

        Pageable limit = PageRequest.of(0, 10);
        List<City> cities = cityRepository.searchByKeyword(keyword, limit);

        return cities.stream()
                .map(city -> {
                    String imageUrl = imageRepository
                            .findImageSizeByTypeAndRelatedId(ImageType.CITY, city.getId(), ImageSizeType.SEARCH)
                            .map(ImageUrl::getUrl)
                            .orElse(null);

                    return CityResponseDto.from(city, imageUrl);
                })
                .collect(Collectors.toList());
    }

    public List<CityResponseDto> getRecommendedCities() {
        List<City> cities = cityRepository.findCitiesOrderByRecommendedScore();

        return cities.stream()
                .map(city -> {
                    String imageUrl = imageRepository
                            .findImageSizeByTypeAndRelatedId(ImageType.CITY, city.getId(), ImageSizeType.SEARCH)
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
                            .findImageSizeByTypeAndRelatedId(ImageType.CITY, dto.id(), ImageSizeType.SEARCH)
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
                            .findImageSizeByTypeAndRelatedId(ImageType.CITY, city.getId(), ImageSizeType.SEARCH)
                            .map(ImageUrl::getUrl)
                            .orElse(null);

                    return CityResponseDto.from(city, imageUrl);
                })
                .collect(Collectors.toList());
    }

}