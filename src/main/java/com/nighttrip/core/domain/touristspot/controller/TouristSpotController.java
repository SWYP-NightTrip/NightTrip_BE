package com.nighttrip.core.domain.touristspot.controller;

import com.nighttrip.core.domain.touristspot.dto.TouristSpotDetailResponse;
import com.nighttrip.core.domain.touristspot.service.TouristSpotService;
import com.nighttrip.core.domain.touristspot.dto.TouristSpotResponseDto;
import com.nighttrip.core.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @GetMapping("/recommend")
    public ResponseEntity<ApiResponse<List<TouristSpotResponseDto>>> getRecommendedTouristSpotsInCity(
            @RequestParam Long cityId) {

        List<TouristSpotResponseDto> recommendedSpots = touristSpotService.getRecommendedTouristSpotsInCity(cityId);
        return ResponseEntity.ok(ApiResponse.success(recommendedSpots));
    }

    @GetMapping("/{touristSpotId}")
    public ResponseEntity<ApiResponse<TouristSpotDetailResponse>> getTouristSpotDetail(
            @PathVariable Long touristSpotId) {
        return ResponseEntity.ok(ApiResponse.success(touristSpotService.getTouristSpotDetail(touristSpotId)));
    }

    @PostMapping("/{touristSpotId}/like")
    public ResponseEntity<ApiResponse<TouristSpotDetailResponse>> addLike(
            @PathVariable Long touristSpotId) {

        touristSpotService.addLike(touristSpotId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }



}