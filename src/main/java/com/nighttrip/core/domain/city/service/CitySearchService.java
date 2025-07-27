package com.nighttrip.core.domain.city.service;
import com.nighttrip.core.domain.city.Implementation.CitySearchServiceImpl;
import com.nighttrip.core.domain.city.dto.CityPopularityDto;
import com.nighttrip.core.domain.city.dto.CityResponseDto;
import com.nighttrip.core.domain.city.entity.City;
import com.nighttrip.core.domain.city.repository.CityRepository;
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

    @Override
    public List<CityResponseDto> searchCity(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("검색어는 비어 있을 수 없습니다.");
        }

        Pageable limit = PageRequest.of(0, 10);
        List<City> cities = cityRepository.searchByKeyword(keyword, limit);

        return cities.stream()
                .map(CityResponseDto::from)
                .toList();
    }

    public List<CityResponseDto> getRecommendedCities() {
        List<City> cities = cityRepository.findCitiesOrderByRecommendedScore();

        return cities.stream()
                .map(CityResponseDto::from)
                .collect(Collectors.toList());
    }
    public List<CityResponseDto> getPopularCities() {
        List<CityPopularityDto> popularCitiesDto = cityRepository.findPopularCitiesWithAggregatedScores();

        return popularCitiesDto.stream()
                .map(dto -> new CityResponseDto(
                        dto.id(), dto.cityName(), dto.imageUrl()))
                .collect(Collectors.toList());
    }

    @Override
    public List<CityResponseDto> getDefaultCities() {
        Pageable pageable = PageRequest.of(0, 7);
        List<City> defaultCities = cityRepository.findAllByOrderByIdAsc(pageable);

        return defaultCities.stream()
                .map(city -> new CityResponseDto(city.getId(), city.getCityName(), city.getImageUrl()))
                .collect(Collectors.toList());
    }

}