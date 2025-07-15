package com.nighttrip.core.oauth.controller;

import com.nighttrip.core.domain.user.entity.User;
import com.nighttrip.core.domain.user.repository.UserRepository;
import com.nighttrip.core.global.dto.ApiResponse;
import com.nighttrip.core.global.enums.ErrorCode;
import com.nighttrip.core.global.exception.BusinessException;
import com.nighttrip.core.oauth.dto.LoginResponseDto;
import com.nighttrip.core.oauth.dto.OAuthUserInfo;
import com.nighttrip.core.oauth.service.GoogleOAuthService;
import com.nighttrip.core.oauth.service.KakaoOAuthService;
import com.nighttrip.core.oauth.service.NaverOAuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/oauth")
@RequiredArgsConstructor
public class OAuthController {

    private final GoogleOAuthService googleOAuthService;
    private final KakaoOAuthService kakaoOAuthService;
    private final NaverOAuthService naverOAuthService;
    private final UserRepository userRepository;
    private final HttpSession httpSession;

    @PostMapping(value = "/login/{provider}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> loginFormEncoded(@PathVariable String provider,
                                              @RequestParam("code") String code) {
        return handleLogin(provider, code);
    }

    @PostMapping(value = "/login/{provider}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> loginJson(@PathVariable String provider,
                                       @RequestBody Map<String, String> body) {
        String code = body.get("code");
        return handleLogin(provider, code);
    }

    private ResponseEntity<?> handleLogin(String provider, String code) {
        if (code == null || code.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_OAUTH_CODE_MISSING);
        }

        OAuthUserInfo userInfo = switch (provider.toUpperCase()) {
            case "GOOGLE" -> googleOAuthService.getUserInfo(code);
            case "KAKAO" -> kakaoOAuthService.getUserInfo(code);
            case "NAVER" -> naverOAuthService.getUserInfo(code);
            default -> throw new BusinessException(ErrorCode.UNSUPPORTED_SOCIAL_PROVIDER);
        };

        Optional<User> optionalUser = userRepository.findByEmail(userInfo.getEmail());
        User user = optionalUser.map(existingUser -> {
            if (existingUser.getProvider() != userInfo.getProvider()) {
                throw new BusinessException(ErrorCode.EMAIL_ALREADY_REGISTERED);
            }
            return existingUser;
        }).orElseGet(() -> userRepository.save(new User(
                userInfo.getEmail(),
                userInfo.getNickname(),
                userInfo.getSocialId(),
                userInfo.getProvider()
        )));



        LoginResponseDto dto = new LoginResponseDto(user.getId(), user.getEmail(), user.getNickname());
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        httpSession.invalidate();
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
