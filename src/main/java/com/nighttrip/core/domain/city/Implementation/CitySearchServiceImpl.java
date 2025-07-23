package com.nighttrip.core.domain.city.Implementation;

import com.nighttrip.core.domain.city.dto.CityResponseDto;
import java.util.List;

public interface CitySearchServiceImpl {
    List<CityResponseDto> searchCity(String keyword);
    List<CityResponseDto> getDomesticCities();
    List<CityResponseDto> getInternationalCities();
    List<CityResponseDto> getPopularCities();
    List<CityResponseDto> getRecommendedCities();
}