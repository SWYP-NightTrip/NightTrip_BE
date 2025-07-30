package com.nighttrip.core.oauth.controller;

import com.nighttrip.core.global.dto.ApiResponse;
import com.nighttrip.core.oauth.dto.LoginStatusResponse;
import com.nighttrip.core.oauth.service.OAuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/oauth")
@RequiredArgsConstructor
@Slf4j
public class OAuthController {

    private final HttpSession httpSession;
    private final OAuthService oAuthService;

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<LoginStatusResponse>> checkLoginStatus() {
        log.info(">>>> [API-CHECK-START] /api/v1/oauth/status 컨트롤러가 호출되었습니다.");

        LoginStatusResponse response = oAuthService.getLoginStatus();
        log.info(">>>> 로그인 상태 응답: {}", response);
        log.info(">>>> [API-CHECK-END] /api/v1/oauth/status 컨트롤러가 응답을 반환합니다.");
        return ResponseEntity.ok(ApiResponse.success(response));
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        httpSession.invalidate();
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
