    package com.nighttrip.core.domain.city.controller;

import com.nighttrip.core.domain.city.service.CitySearchService;
import com.nighttrip.core.domain.city.dto.CityResponseDto;
import com.nighttrip.core.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/city")
public class CitySearchController {


    private final CitySearchService citySearchService;
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CityResponseDto>>> searchCity(@RequestParam String keyword) {
        List<CityResponseDto> cities = citySearchService.searchCity(keyword);
        return ResponseEntity.ok(ApiResponse.success(cities));
    }

    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<CityResponseDto>>> getPopularCities() {
        List<CityResponseDto> cities = citySearchService.getPopularCities();
        return ResponseEntity.ok(ApiResponse.success(cities));
    }
    @GetMapping("/default")
    public ResponseEntity<ApiResponse<List<CityResponseDto>>> getDefaultCities() {
        List<CityResponseDto> defaultCities = citySearchService.getDefaultCities();
        return ResponseEntity.ok(ApiResponse.success(defaultCities));
    }


    @GetMapping("/recommend/popular")
    public ResponseEntity<ApiResponse<List<CityResponseDto>>> getRecommendedCities() {
        List<CityResponseDto> cities = citySearchService.getRecommendedCities();
        return ResponseEntity.ok(ApiResponse.success(cities));
    }

    @GetMapping("/recommend/popular/all")
    public ResponseEntity<ApiResponse<Page<CityResponseDto>>> getPopularCitiesAll(
            @PageableDefault(size = 6) Pageable pageable
    ) {
        Page<CityResponseDto> citiesPage = citySearchService.getPopularCitiesAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(citiesPage));
    }

    @GetMapping("/recommend/monthly")
    public ResponseEntity<ApiResponse<List<CityResponseDto>>> getMonthlyTrendingCities(
            @RequestParam int year, @RequestParam int month) {
        List<CityResponseDto> cities = citySearchService.getMonthlyTrendingCities(year, month);
        return ResponseEntity.ok(ApiResponse.success(cities));
    }

    @GetMapping("/recommend/monthly/all")
    public ResponseEntity<ApiResponse<Page<CityResponseDto>>> getMonthlyTrendingCitiesAll(
            @RequestParam int year,
            @RequestParam int month,
            @PageableDefault(size = 6) Pageable pageable) {
        Page<CityResponseDto> citiesPage = citySearchService.getMonthlyTrendingCitiesAll(year, month, pageable);
        return ResponseEntity.ok(ApiResponse.success(citiesPage));
    }
}
