package com.nighttrip.core.oauth.controller;

import com.nighttrip.core.global.dto.ApiResponse;
import com.nighttrip.core.oauth.dto.LoginStatusResponse;
import com.nighttrip.core.oauth.service.OAuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
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
        LoginStatusResponse loginStatus = oAuthService.getLoginStatus();
        return ResponseEntity.ok(ApiResponse.success(loginStatus));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        httpSession.invalidate();
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
