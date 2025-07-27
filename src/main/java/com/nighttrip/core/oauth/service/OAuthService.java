package com.nighttrip.core.oauth.service;

import com.nighttrip.core.domain.user.dto.UserInfoResponse;
import com.nighttrip.core.domain.user.entity.User;
import com.nighttrip.core.domain.user.repository.UserRepository;
import com.nighttrip.core.oauth.dto.LoginStatusResponse;
import com.nighttrip.core.oauth.util.SecurityUtils;
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
                .map(this::createLoggedInResponse)
                .orElseGet(this::createLoggedOutResponse);
    }

    private LoginStatusResponse createLoggedInResponse(User user) {
        UserInfoResponse userInfo = new UserInfoResponse(user);
        return new LoginStatusResponse(true, userInfo);
    }

    private LoginStatusResponse createLoggedOutResponse() {
        return new LoginStatusResponse(false, null);
    }
}
