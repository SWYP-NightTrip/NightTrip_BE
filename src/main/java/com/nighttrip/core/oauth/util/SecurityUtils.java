package com.nighttrip.core.oauth.util;

import com.nighttrip.core.global.enums.ErrorCode;
import com.nighttrip.core.global.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;
import java.util.Optional;

public class SecurityUtils {
    public static String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            throw new BusinessException(ErrorCode.USER_UNAUTHORIZED);
        }

        Object principal = auth.getPrincipal();

        if (!(auth instanceof OAuth2AuthenticationToken oauthToken)) {
            throw new BusinessException(ErrorCode.USER_UNAUTHORIZED);
        }

        String registrationId = oauthToken.getAuthorizedClientRegistrationId();
        OAuth2User oauth2User = (OAuth2User) principal;

        String email;

        switch (registrationId.toLowerCase()) {
            case "google":
                email = oauth2User.getAttribute("email");
                break;
            case "kakao":
                Map<String, Object> kakaoAccount = oauth2User.getAttribute("kakao_account");
                if (kakaoAccount == null) throw new BusinessException(ErrorCode.USER_UNAUTHORIZED);
                email = (String) kakaoAccount.get("email");
                break;
            case "naver":
                Map<String, Object> response = oauth2User.getAttribute("response");
                if (response == null) throw new BusinessException(ErrorCode.USER_UNAUTHORIZED);
                email = (String) response.get("email");
                break;
            default:
                throw new BusinessException(ErrorCode.USER_UNAUTHORIZED);
        }

        if (email == null || email.isEmpty()) {
            throw new BusinessException(ErrorCode.USER_UNAUTHORIZED);
        }

        return email;
    }

    public static Optional<String> findCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return Optional.empty();
        }

        if (!(auth instanceof OAuth2AuthenticationToken oauthToken)) {
            return Optional.empty();
        }

        String registrationId = oauthToken.getAuthorizedClientRegistrationId();
        OAuth2User oauth2User = (OAuth2User) auth.getPrincipal();

        String email = switch (registrationId.toLowerCase()) {
            case "google" -> oauth2User.getAttribute("email");
            case "kakao" -> {
                Map<String, Object> kakaoAccount = oauth2User.getAttribute("kakao_account");
                yield kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
            }
            case "naver" -> {
                Map<String, Object> response = oauth2User.getAttribute("response");
                yield response != null ? (String) response.get("email") : null;
            }
            default -> null;
        };

        return Optional.ofNullable(email);
    }
}