package com.nighttrip.core.ai.controller;

import com.nighttrip.core.ai.dto.UserContext;
import com.nighttrip.core.ai.service.AiTouristSpotReadService;
import com.nighttrip.core.ai.service.SpotRerankService;
import com.nighttrip.core.domain.touristspot.dto.RecommendTouristSpotResponse;
import com.nighttrip.core.domain.touristspot.dto.TouristSpotDetailResponse;
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
    private final AiTouristSpotReadService aiSpotService;

    @PostMapping("/sections")
    public ResponseEntity<ApiResponse<List<RecommendTouristSpotResponse>>> recommend(
            @RequestBody UserContext user) {

        var list = rerankService.recommendSectionItems(user);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/spots/{id}")
    public ResponseEntity<ApiResponse<TouristSpotDetailResponse>> spot(@PathVariable Long id) {
        var detail = aiSpotService.getDetail(id);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

}
