package com.nighttrip.core.main.controller;

import com.nighttrip.core.domain.user.entity.User;
import com.nighttrip.core.domain.user.service.UserService;
import com.nighttrip.core.global.dto.ApiResponse;
import com.nighttrip.core.main.dto.RecommendedSpotDto;
import com.nighttrip.core.main.service.MainPageService;
import com.nighttrip.core.oauth.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/main/recommend")
public class MainPageController {

    private final MainPageService mainPageService;
    private final UserService userService;

    @GetMapping("/night-popular")
    public ApiResponse<List<RecommendedSpotDto>> getNightPopularSpots(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon) {

        Optional<String> userEmailOpt = SecurityUtils.findCurrentUserEmail();

        User user = userEmailOpt
                .flatMap(userService::findUserByEmail)
                .orElse(null);

        List<RecommendedSpotDto> spots = mainPageService.getNightPopularSpots(user, lat, lon);
        return ApiResponse.success(spots);
    }

    @GetMapping("/category")
    public ApiResponse<List<RecommendedSpotDto>> getCategoryRecommendedSpots(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon) {

        Optional<String> userEmailOpt = SecurityUtils.findCurrentUserEmail();

        User user = userEmailOpt
                .flatMap(userService::findUserByEmail)
                .orElse(null);

        List<RecommendedSpotDto> spots = mainPageService.getCategoryRecommendedSpots(user, lat, lon);
        return ApiResponse.success(spots);
    }
}
