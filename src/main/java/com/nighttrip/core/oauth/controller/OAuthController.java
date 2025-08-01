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
import org.springframework.http.HttpStatus;
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
        // [1] 메서드 진입점 로그
        log.info(">>>> [checkLoginStatus] 로그인 상태 확인 요청이 들어왔습니다.");

        LoginStatusResponse loginStatus;
        try {
            // [2] oAuthService 호출 전 로그
            log.info(">>>> [checkLoginStatus] oAuthService.getLoginStatus() 호출을 시도합니다.");

            loginStatus = oAuthService.getLoginStatus();

            // [3] oAuthService 호출 성공 후 결과 로그
            log.info(">>>> [checkLoginStatus] oAuthService.getLoginStatus() 호출 성공!");
            log.info(">>>> [checkLoginStatus] LoginStatusResponse 객체 전체: {}", loginStatus);

        } catch (Exception e) {
            // [4] 예외 발생 시 로그
            log.error(">>>> [checkLoginStatus] oAuthService.getLoginStatus() 호출 중 예외 발생: {}", e.getMessage(), e);
            // 예외가 발생하면 로그인 실패로 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("로그인 상태 확인 중 서버 내부 오류가 발생했습니다."));
        }

        // [5] LoginStatusResponse 객체 내부 값 상세 로깅
        log.info(">>>> [checkLoginStatus] isLoggedIn 값: {}", loginStatus.isLoggedIn());

        if (loginStatus.isLoggedIn()) {
            UserInfoResponse userInfo = loginStatus.getUserInfo();
            if (userInfo != null) {
                // [6] UserInfoResponse 객체 값 상세 로깅
                log.info(">>>> [checkLoginStatus] userInfo 객체 상세 정보:");
                log.info(">>>> [checkLoginStatus] User ID: {}", userInfo.getUserId());
                log.info(">>>> [checkLoginStatus] Email: {}", userInfo.getEmail());
                log.info(">>>> [checkLoginStatus] Nickname: {}", userInfo.getNickname());
                // 필요한 경우, UserInfoResponse에 추가된 다른 필드도 이곳에 로깅할 수 있습니다.
            } else {
                log.warn(">>>> [checkLoginStatus] 로그인 상태이지만 userInfo 객체가 null입니다.");
            }
        } else {
            // [7] 비로그인 상태 로그
            log.info(">>>> [checkLoginStatus] 현재 로그인 상태가 아닙니다.");
        }

        // [8] 메서드 종료 직전 로그
        log.info(">>>> [checkLoginStatus] 로그인 상태 확인 프로세스 종료.");

        return ResponseEntity.ok(ApiResponse.success(loginStatus));
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        httpSession.invalidate();
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
