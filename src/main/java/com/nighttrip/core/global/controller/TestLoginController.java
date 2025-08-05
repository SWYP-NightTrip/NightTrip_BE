package com.nighttrip.core.global.controller;// TestLoginController.java
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession; // HttpSession 임포트 추가
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import com.nighttrip.core.domain.user.entity.User;
import com.nighttrip.core.domain.user.repository.UserRepository;
import com.nighttrip.core.global.dto.ApiResponse;
import com.nighttrip.core.global.dto.CustomUserDetails;
import com.nighttrip.core.global.oauth.dto.LoginStatusResponse;
import com.nighttrip.core.domain.user.dto.UserInfoResponse;


@RestController
@RequestMapping("/api/v1/test")
public class TestLoginController {

    private final UserRepository userRepository;

    public TestLoginController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/login")
    public ResponseEntity<ApiResponse<LoginStatusResponse>> testLogin(
            HttpServletResponse httpServletResponse,
            HttpSession session
    ) {
        User user = userRepository.findById(2L)
                .orElseThrow(() -> new IllegalArgumentException("User with ID 1 not found"));

        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
        UserInfoResponse userInfo = new UserInfoResponse(user, "http://iolkjasf.img");
        LoginStatusResponse response = new LoginStatusResponse(true, userInfo);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

}