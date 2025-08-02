package com.nighttrip.core.global.oauth.userinfo;

import com.nighttrip.core.global.enums.OauthProvider;

import java.util.Map;

public class GoogleUserInfo implements OAuthUserInfo {

    private final Map<String, Object> attributes;

    public GoogleUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getNickname() {
        return (String) attributes.get("name");
    }

    @Override
    public String getSocialId() {
        return (String) attributes.get("sub");
    }

    @Override
    public OauthProvider getProvider() {
        return OauthProvider.GOOGLE;
    }
}
