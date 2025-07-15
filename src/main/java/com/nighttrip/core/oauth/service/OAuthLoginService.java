package com.nighttrip.core.oauth.service;

import com.nighttrip.core.oauth.dto.OAuthUserInfo;

public interface OAuthLoginService {
    OAuthUserInfo getUserInfo(String code);
}
