package com.nighttrip.core.mypage.controller;

import com.nighttrip.core.global.dto.ApiResponse;
import com.nighttrip.core.mypage.dto.MyPageResponseDto;
import com.nighttrip.core.mypage.service.MyPageService;
import com.nighttrip.core.oauth.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
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
}
