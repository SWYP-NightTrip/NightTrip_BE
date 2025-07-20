package com.nighttrip.core.domain.city.service;
import com.nighttrip.core.domain.city.Implementation.CitySearchServiceImpl;
import com.nighttrip.core.domain.city.dto.CityResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CitySearchService implements CitySearchServiceImpl {

    @Override
    public List<CityResponseDto> searchCity(String keyword) {
        // 검색 로직 구현
        return List.of(); // 예시
    }

    @Override
    public List<CityResponseDto> getDomesticCities() {
        return List.of();
    }

    @Override
    public List<CityResponseDto> getInternationalCities() {
        return List.of();
    }

    @Override
    public List<CityResponseDto> getPopularCities() {
        return List.of();
    }

    @Override
    public List<CityResponseDto> getRecommendedCities() {
        return List.of();
    }
}