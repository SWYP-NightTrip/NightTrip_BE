package com.nighttrip.core.oauth.controller;

import com.nighttrip.core.domain.user.dto.UserInfoResponse;
import com.nighttrip.core.global.dto.ApiResponse;
import com.nighttrip.core.oauth.dto.LoginStatusResponse;
import com.nighttrip.core.oauth.service.OAuthService;
import jakarta.servlet.http.HttpServletRequest; // HttpServletRequest 임포트
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthService oAuthService;

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<LoginStatusResponse>> checkLoginStatus(HttpServletRequest request) { // HttpServletRequest를 인자로 받습니다.
        log.info(">>>> [checkLoginStatus] 로그인 상태 확인 요청이 들어왔습니다.");

        // [1] 쿠키 헤더 로그를 추가합니다.
        String cookieHeader = request.getHeader("Cookie");
        if (cookieHeader != null) {
            log.info(">>>> [checkLoginStatus] Request to /status includes Cookie header: {}", cookieHeader);
        } else {
            log.warn(">>>> [checkLoginStatus] Request to /status does NOT include Cookie header.");
        }

        LoginStatusResponse loginStatus;
        try {
            log.info(">>>> [checkLoginStatus] oAuthService.getLoginStatus() 호출을 시도합니다.");
            loginStatus = oAuthService.getLoginStatus();
            log.info(">>>> [checkLoginStatus] oAuthService.getLoginStatus() 호출 성공!");
            log.info(">>>> [checkLoginStatus] LoginStatusResponse 객체 전체: {}", loginStatus);
        } catch (Exception e) {
            log.error(">>>> [checkLoginStatus] oAuthService.getLoginStatus() 호출 중 예외 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("로그인 상태 확인 중 서버 내부 오류가 발생했습니다."));
        }

        log.info(">>>> [checkLoginStatus] isLoggedIn 값: {}", loginStatus.isLoggedIn());

        if (loginStatus.isLoggedIn()) {
            UserInfoResponse userInfo = loginStatus.getUserInfo();
            if (userInfo != null) {
                log.info(">>>> [checkLoginStatus] userInfo 객체 상세 정보:");
                log.info(">>>> [checkLoginStatus] User ID: {}", userInfo.getUserId());
                log.info(">>>> [checkLoginStatus] Email: {}", userInfo.getEmail());
                log.info(">>>> [checkLoginStatus] Nickname: {}", userInfo.getNickname());
            } else {
                log.warn(">>>> [checkLoginStatus] 로그인 상태이지만 userInfo 객체가 null입니다.");
            }
        } else {
            log.info(">>>> [checkLoginStatus] 현재 로그인 상태가 아닙니다.");
        }

        log.info(">>>> [checkLoginStatus] 로그인 상태 확인 프로세스 종료.");
        return ResponseEntity.ok(ApiResponse.success(loginStatus));
    }
}