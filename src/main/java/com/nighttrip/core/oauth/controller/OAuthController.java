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
    public ResponseEntity<ApiResponse<LoginStatusResponse>> checkLoginStatus(HttpServletRequest request) { // HttpServletRequest 추가
        log.info(">>>> [API-CHECK-START] /api/v1/oauth/status 컨트롤러가 호출되었습니다.");

        // 1. Authorization 헤더 로깅
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null) {
            log.info(">>>> 요청 Authorization 헤더: {}", authorizationHeader);
        } else {
            log.info(">>>> 요청 Authorization 헤더: 없음");
        }

        // 2. 모든 쿠키 로깅
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            log.info(">>>> 요청 쿠키 정보:");
            for (Cookie cookie : cookies) {
                log.info(">>>> 쿠키 이름: {}, 값: {}", cookie.getName(), cookie.getValue());
                // 특정 쿠키(예: JSESSIONID)만 보고 싶다면 아래처럼 조건 추가
                // if ("JSESSIONID".equals(cookie.getName())) {
                //     log.info(">>>> JSESSIONID 쿠키 값: {}", cookie.getValue());
                // }
            }
        } else {
            log.info(">>>> 요청 쿠키: 없음");
        }

        LoginStatusResponse response = oAuthService.getLoginStatus();

        // 기존 로그인 상태 응답 로깅
        log.info(">>>> 로그인 상태 응답 - isLoggedIn: {}", response.isLoggedIn());
        if (response.getUserInfo() != null) {
            log.info(">>>> 로그인 상태 응답 - UserInfo: {}", response.getUserInfo());
        } else {
            log.info(">>>> 로그인 상태 응답 - UserInfo: null (사용자 정보 없음)");
        }

        log.info(">>>> [API-CHECK-END] /api/v1/oauth/status 컨트롤러가 응답을 반환합니다.");
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        httpSession.invalidate();
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
