package com.nighttrip.core.global.oauth.userinfo;

import com.nighttrip.core.global.enums.OauthProvider;

import java.util.Map;

public class NaverUserInfo implements OAuthUserInfo {

    private final Map<String, Object> attributes;

    public NaverUserInfo(Map<String, Object> attributes) {
        this.attributes = (Map<String, Object>) attributes.get("response");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getNickname() {
        return (String) attributes.get("nickname");
    }

    @Override
    public String getSocialId() {
        return (String) attributes.get("id");
    }

    @Override
    public OauthProvider getProvider() {
        return OauthProvider.NAVER;
    }
}
