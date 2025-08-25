package com.nighttrip.core.global.ai.controller;

import com.nighttrip.core.global.ai.dto.RerankResult;
import com.nighttrip.core.global.ai.dto.UserContext;
import com.nighttrip.core.global.ai.service.SpotRerankService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final SpotRerankService rerankService;

    @PostMapping("/city/{cityId}")
    public ResponseEntity<?> recommend(
            @PathVariable Long cityId,
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "12") double radiusKm,
            @RequestBody UserContext user) throws Exception {

        RerankResult result = rerankService.recommend(cityId, lat, lng, radiusKm, user);
        return ResponseEntity.ok(result);
    }
}
