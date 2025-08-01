// src/main/java/com/nighttrip/core/global/controller/TestLoginController.java

package com.nighttrip.core.global.controller;

import com.nighttrip.core.domain.user.entity.User;
import com.nighttrip.core.domain.user.repository.UserRepository;
import com.nighttrip.core.global.dto.ApiResponse;
import com.nighttrip.core.global.dto.CustomUserDetails;
import com.nighttrip.core.oauth.dto.LoginStatusResponse;
import com.nighttrip.core.domain.user.dto.UserInfoResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity; // ResponseEntity 임포트 추가
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class TestLoginController {

    private final UserRepository userRepository;

    @GetMapping("/login")
    public ResponseEntity<ApiResponse<LoginStatusResponse>> testLogin(HttpServletResponse httpServletResponse) { // 반환 타입 변경
        User user = userRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("User with ID 1 not found"));

        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.info(">>>> Test login successful. Session created for user: {}", user.getEmail());

        UserInfoResponse userInfo = new UserInfoResponse(user);

        LoginStatusResponse response = new LoginStatusResponse(true, userInfo);
            log.info(">>>> Test login successful. Session created for user: {}", user.getEmail());

            // 이 부분에서 응답 헤더를 확인하는 로그를 추가해 보세요.
            Collection<String> headers = httpServletResponse.getHeaders("Set-Cookie");
            if (headers != null && !headers.isEmpty()) {
                log.info(">>>> Spring Server Response Headers: Set-Cookie is present with value: {}", headers);
            } else {
                log.warn(">>>> Spring Server Response Headers: Set-Cookie header is NOT present.");
            }

            // ... (기존 코드)
            return ResponseEntity.ok(ApiResponse.success(response));
        }

}