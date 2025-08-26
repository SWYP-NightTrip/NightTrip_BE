package com.nighttrip.core.domain.tripplan.controller;

import com.nighttrip.core.domain.tripplan.service.SpotRecommendationService;
import com.nighttrip.core.feature.mainpage.dto.CategoryRecommendationDto;
import com.nighttrip.core.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/trip-plan/{tripPlanId}/spot-recommendations")
public class SpotRecommendationController {

    private final SpotRecommendationService spotRecommendationService;

    @GetMapping
    public ApiResponse<CategoryRecommendationDto> getSpotsByCategory(
            @PathVariable Long tripPlanId,
            @RequestParam("type") String category,
            @PageableDefault(size = 3) Pageable pageable) {

        CategoryRecommendationDto result = spotRecommendationService.getSpotsByCategoryPaginated(tripPlanId, category, pageable);
        return ApiResponse.success(result);
    }
}