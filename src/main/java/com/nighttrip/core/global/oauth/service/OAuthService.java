package com.nighttrip.core.global.oauth.service;

import com.nighttrip.core.domain.user.dto.UserInfoResponse;
import com.nighttrip.core.domain.user.entity.User;
import com.nighttrip.core.domain.user.repository.UserRepository;
import com.nighttrip.core.global.enums.ErrorCode;
import com.nighttrip.core.global.enums.ImageType;
import com.nighttrip.core.global.exception.BusinessException;
import com.nighttrip.core.global.image.entity.ImageUrl;
import com.nighttrip.core.global.image.repository.ImageRepository;
import com.nighttrip.core.global.oauth.dto.LoginStatusResponse;
import com.nighttrip.core.global.oauth.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OAuthService {

    private final UserRepository userRepository;
    private final ImageRepository imageRepository;

    public LoginStatusResponse getLoginStatus() {
        Optional<String> emailOpt = SecurityUtils.findCurrentUserEmail();

        if (emailOpt.isEmpty()) {
            log.warn("❌ 인증된 사용자 이메일을 가져올 수 없음 (SecurityContext 문제)");
            throw new BusinessException(ErrorCode.INVALID_UNAUTHORIZED);
        }

        String email = emailOpt.get();
        log.info("✅ 현재 로그인 이메일: {}", email);

        return userRepository.findByEmail(email)
                .map(user -> {
                    String avatarUrl = imageRepository.findTHUMBNAILImage(String.valueOf(ImageType.AVATAR), user.getId())
                            .map(ImageUrl::getUrl)
                            .orElse(null);

                    UserInfoResponse userInfo = new UserInfoResponse(user, avatarUrl);
                    return new LoginStatusResponse(true, userInfo);
                })
                .orElseThrow(() -> {
                    log.warn("❌ 해당 이메일을 가진 유저를 찾을 수 없음: {}", email);
                    return new BusinessException(ErrorCode.INVALID_UNAUTHORIZED);
                });
    }

    private LoginStatusResponse createLoggedInResponse(User user) {
        String  url = imageRepository.findTHUMBNAILImage(String.valueOf(ImageType.AVATAR), user.getId())
                .map(ImageUrl::getUrl)
                .orElse(null);
        UserInfoResponse userInfo = new UserInfoResponse(user, url);
        return new LoginStatusResponse(true, userInfo);
    }

    private LoginStatusResponse createLoggedOutResponse() {
        return new LoginStatusResponse(false, null);
    }

    public void logout(HttpServletRequest request) {

        jakarta.servlet.http.HttpSession session = request.getSession(false);

        if (session != null) {
            log.info(">>>> [logout] 세션을 무효화합니다. Session ID: {}", session.getId());
            session.invalidate();
        } else {
            log.warn(">>>> [logout] 무효화할 세션이 존재하지 않습니다.");
        }

        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        log.info(">>>> [logout] SecurityContextHolder가 초기화되었습니다.");
    }
}
