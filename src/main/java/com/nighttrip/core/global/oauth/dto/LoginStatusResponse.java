package com.nighttrip.core.global.oauth.dto;

import com.nighttrip.core.domain.user.dto.UserInfoResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LoginStatusResponse {
    private final boolean isLoggedIn;
    private final UserInfoResponse userInfo;
}
