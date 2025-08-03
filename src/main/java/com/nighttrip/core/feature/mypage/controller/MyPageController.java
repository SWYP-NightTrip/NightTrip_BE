package com.nighttrip.core.feature.mypage.controller;

import com.nighttrip.core.feature.mypage.dto.LikedSpotDto;
import com.nighttrip.core.feature.mypage.dto.MyPageResponseDto;
import com.nighttrip.core.feature.mypage.service.MyPageService;
import com.nighttrip.core.global.dto.ApiResponse;
import com.nighttrip.core.global.oauth.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping
    public ApiResponse<MyPageResponseDto> getMyPageInfo() {

        String userEmail = SecurityUtils.getCurrentUserEmail();
        MyPageResponseDto myPageData = myPageService.getMyPageData(userEmail);
        return ApiResponse.success(myPageData);
    }

    @GetMapping("/likes")
    public ApiResponse<Page<LikedSpotDto>> getLikedSpots(
            @PageableDefault(size = 5) Pageable pageable
    ) {
        String userEmail = SecurityUtils.getCurrentUserEmail();

        Page<LikedSpotDto> likedSpots = myPageService.getLikedSpots(userEmail, pageable);

        return ApiResponse.success(likedSpots);
    }
}
