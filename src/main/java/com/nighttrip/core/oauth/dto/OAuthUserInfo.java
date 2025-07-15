package com.nighttrip.core.oauth.dto;

import com.nighttrip.core.global.enums.Oauth_Provider;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OAuthUserInfo {
    private String email;
    private String nickname;
    private String socialId;
    private Oauth_Provider provider;
}