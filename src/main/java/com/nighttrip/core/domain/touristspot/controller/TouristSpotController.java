package com.nighttrip.core.domain.touristspot.controller;

import com.nighttrip.core.domain.touristspot.Implementation.TouristSpotService;
import com.nighttrip.core.domain.touristspot.dto.TouristSpotResponseDto;
import com.nighttrip.core.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/touristspot")
@RequiredArgsConstructor
public class TouristSpotController {

    private final TouristSpotService touristSpotService;


    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<TouristSpotResponseDto>>> getPopularTouristSpotsInCity(
            @RequestParam Long cityId) {

        List<TouristSpotResponseDto> popularSpots = touristSpotService.getPopularTouristSpotsInCity(cityId);
        return ResponseEntity.ok(ApiResponse.success(popularSpots));
    }

    @GetMapping("/recommended")
    public ResponseEntity<ApiResponse<List<TouristSpotResponseDto>>> getRecommendedTouristSpotsInCity(
            @RequestParam Long cityId) {

        List<TouristSpotResponseDto> recommendedSpots = touristSpotService.getRecommendedTouristSpotsInCity(cityId);
        return ResponseEntity.ok(ApiResponse.success(recommendedSpots));
    }


}