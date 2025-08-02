package com.nighttrip.core.global.oauth.service;

import com.nighttrip.core.domain.user.dto.UserInfoResponse;
import com.nighttrip.core.domain.user.entity.User;
import com.nighttrip.core.domain.user.repository.UserRepository;
import com.nighttrip.core.global.enums.ErrorCode;
import com.nighttrip.core.global.exception.BusinessException;
import com.nighttrip.core.global.oauth.dto.LoginStatusResponse;
import com.nighttrip.core.global.oauth.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OAuthService {

    private final UserRepository userRepository;

    public LoginStatusResponse getLoginStatus() {
        return SecurityUtils.findCurrentUserEmail()
                .flatMap(userRepository::findByEmailWithAvatar)
                .map(user -> createLoggedInResponse(user))
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_UNAUTHORIZED));
    }

    private LoginStatusResponse createLoggedInResponse(User user) {
        UserInfoResponse userInfo = new UserInfoResponse(user);
        return new LoginStatusResponse(true, userInfo);
    }

    private LoginStatusResponse createLoggedOutResponse() {
        return new LoginStatusResponse(false, null);
    }
}
