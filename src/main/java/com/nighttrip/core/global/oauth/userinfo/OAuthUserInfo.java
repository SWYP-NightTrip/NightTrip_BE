package com.nighttrip.core.global.oauth.userinfo;

import com.nighttrip.core.global.enums.OauthProvider;

public interface OAuthUserInfo {
    String getEmail();
    String getNickname();
    String getSocialId();
    OauthProvider getProvider();
}