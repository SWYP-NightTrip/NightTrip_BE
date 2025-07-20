    package com.nighttrip.core.domain.city.controller;

import com.nighttrip.core.domain.city.service.CitySearchService;
import com.nighttrip.core.domain.city.dto.CityResponseDto;
import com.nighttrip.core.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
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

    // 국내 도시
    @GetMapping("/domestic")
    public ResponseEntity<ApiResponse<List<CityResponseDto>>> getDomesticCities() {
        List<CityResponseDto> cities = citySearchService.getDomesticCities();
        return ResponseEntity.ok(ApiResponse.success(cities));
    }

    // 해외 도시
    @GetMapping("/international")
    public ResponseEntity<ApiResponse<List<CityResponseDto>>> getInternationalCities() {
        List<CityResponseDto> cities = citySearchService.getInternationalCities();
        return ResponseEntity.ok(ApiResponse.success(cities));
    }

    // 인기 도시 순
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<CityResponseDto>>> getPopularCities() {
        List<CityResponseDto> cities = citySearchService.getPopularCities();
        return ResponseEntity.ok(ApiResponse.success(cities));
    }

    // 추천 도시 순
    @GetMapping("/recommended")
    public ResponseEntity<ApiResponse<List<CityResponseDto>>> getRecommendedCities() {
        List<CityResponseDto> cities = citySearchService.getRecommendedCities();
        return ResponseEntity.ok(ApiResponse.success(cities));
    }
}
