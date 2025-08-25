package com.nighttrip.core.global.ai.controller;

import com.nighttrip.core.domain.touristspot.dto.RecommendTouristSpotResponse;
import com.nighttrip.core.global.ai.dto.RerankResult;
import com.nighttrip.core.global.ai.dto.UserContext;
import com.nighttrip.core.global.ai.service.SpotRerankService;
import com.nighttrip.core.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final SpotRerankService rerankService;

    @PostMapping("/city/{cityId}")
    public ResponseEntity<ApiResponse<List<RecommendTouristSpotResponse>>> recommend(
            @PathVariable Long cityId,
            @RequestBody UserContext user) {

        RerankResult result = rerankService.recommend(cityId, user);
        return ResponseEntity.ok(
                ApiResponse.success(
                        rerankService.toRankedDtos(result)
                ));
    }
}
