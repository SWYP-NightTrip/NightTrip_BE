package com.nighttrip.core.oauth.controller;

import com.nighttrip.core.domain.user.dto.UserInfoResponse;
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
        // [1] oAuthService.getLoginStatus() 메서드 호출 전 로그
        log.info(">>>> [checkLoginStatus] 로그인 상태 확인 요청이 들어왔습니다.");

        LoginStatusResponse loginStatus = oAuthService.getLoginStatus();

        // [2] LoginStatusResponse 객체 전체 값 로깅
        log.info(">>>> [checkLoginStatus] oAuthService.getLoginStatus() 호출 결과: {}", loginStatus);

        // [3] LoginStatusResponse 객체 내부 값 상세 로깅
        log.info(">>>> [checkLoginStatus] isLoggedIn 값: {}", loginStatus.isLoggedIn());

        if (loginStatus.isLoggedIn()) {
            UserInfoResponse userInfo = loginStatus.getUserInfo();

            log.info(">>>> [checkLoginStatus] userInfo 객체가 존재합니다. 상세 정보 로깅을 시작합니다.");
            log.info(">>>> [checkLoginStatus] User ID: {}", userInfo.getUserId());
            log.info(">>>> [checkLoginStatus] Email: {}", userInfo.getEmail());
            log.info(">>>> [checkLoginStatus] Nickname: {}", userInfo.getNickname());
            // 필요한 경우 다른 UserInfoResponse 필드도 추가로 로깅 가능
        } else {
            log.info(">>>> [checkLoginStatus] 현재 로그인 상태가 아닙니다.");
        }

        return ResponseEntity.ok(ApiResponse.success(loginStatus));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        httpSession.invalidate();
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
