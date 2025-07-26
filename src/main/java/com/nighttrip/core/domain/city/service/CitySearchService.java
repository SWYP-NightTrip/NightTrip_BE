package com.nighttrip.core.domain.city.service;
import com.nighttrip.core.domain.city.Implementation.CitySearchServiceImpl;
import com.nighttrip.core.domain.city.dto.CityResponseDto;
import com.nighttrip.core.domain.city.entity.City;
import com.nighttrip.core.domain.city.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CitySearchService implements CitySearchServiceImpl {

    @Override
    public List<CityResponseDto> searchCity(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("검색어는 비어 있을 수 없습니다.");
        }

        Pageable limit = PageRequest.of(0, 10); // 최대 10개 제한
        List<City> cities = cityRepository.searchByKeyword(keyword, limit);

        return cities.stream()
                .map(CityResponseDto::from)
                .toList();
    }

    private final CityRepository cityRepository;

    @Override
    public List<CityResponseDto> getPopularCities() {
        return List.of();
    }

    @Override
    public List<CityResponseDto> getRecommendedCities() {
        return List.of();
    }
}