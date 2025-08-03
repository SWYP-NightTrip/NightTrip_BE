package com.nighttrip.core.feature.mainpage.controller;

import com.nighttrip.core.domain.user.entity.User;
import com.nighttrip.core.domain.user.service.UserService;
import com.nighttrip.core.feature.mainpage.dto.CategoryRecommendationDto;
import com.nighttrip.core.global.dto.ApiResponse;
import com.nighttrip.core.feature.mainpage.dto.PartnerServiceDto;
import com.nighttrip.core.feature.mainpage.dto.RecommendedSpotDto;
import com.nighttrip.core.feature.mainpage.service.MainPageService;
import com.nighttrip.core.global.oauth.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/main")
public class MainPageController {

    private final MainPageService mainPageService;
    private final UserService userService;

    @GetMapping("/recommend/night-popular")
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

    @GetMapping("/recommend/category")
    public ApiResponse<CategoryRecommendationDto> getCategoryRecommendedSpots(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon) {

        Optional<String> userEmailOpt = SecurityUtils.findCurrentUserEmail();

        User user = userEmailOpt
                .flatMap(userService::findUserByEmail)
                .orElse(null);

        CategoryRecommendationDto result = mainPageService.getCategoryRecommendedSpots(user, lat, lon);
        return ApiResponse.success(result);
    }

    @GetMapping("/recommend/random-category")
    public ApiResponse<CategoryRecommendationDto> getRandomCategorySpots(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon) {

        CategoryRecommendationDto result = mainPageService.getCategoryRecommendedSpots(null, lat, lon);

        return ApiResponse.success(result);
    }

    @GetMapping("/recommend/night-popular/all")
    public ApiResponse<Page<RecommendedSpotDto>> getNightPopularSpotsAll(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @PageableDefault(size = 6) Pageable pageable) {

        User user = SecurityUtils.findCurrentUserEmail()
                .flatMap(userService::findUserByEmail)
                .orElse(null);

        Page<RecommendedSpotDto> spotsPage = mainPageService.getNightPopularSpotsPaginated(user, lat, lon, pageable);
        return ApiResponse.success(spotsPage);
    }

    @GetMapping("/recommend/category/all")
    public ApiResponse<Page<RecommendedSpotDto>> getCategoryRecommendedSpotsAll(
            @RequestParam String category,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @PageableDefault(size = 6) Pageable pageable) {

        User user = SecurityUtils.findCurrentUserEmail()
                .flatMap(userService::findUserByEmail)
                .orElse(null);

        Page<RecommendedSpotDto> spotsPage = mainPageService.getCategoryRecommendedSpotsPaginated(user, lat, lon, category, pageable);
        return ApiResponse.success(spotsPage);
    }

    @GetMapping("/recommend/random-category/all")
    public ApiResponse<Page<RecommendedSpotDto>> getRandomCategorySpotsAll(
            @RequestParam String category,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @PageableDefault(size = 6) Pageable pageable) {

        Page<RecommendedSpotDto> spotsPage = mainPageService.getCategoryRecommendedSpotsPaginated(null, lat, lon, category, pageable);
        return ApiResponse.success(spotsPage);
    }


    @GetMapping("/partner-services")
    public ApiResponse<List<PartnerServiceDto>> getPartnerServices() {
        List<PartnerServiceDto> partnerServices = mainPageService.getPartnerServices();
        return ApiResponse.success(partnerServices);
    }
}
