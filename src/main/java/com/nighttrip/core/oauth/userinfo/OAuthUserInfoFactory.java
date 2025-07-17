package com.nighttrip.core.oauth.userinfo;

import com.nighttrip.core.global.enums.ErrorCode;
import com.nighttrip.core.global.exception.BusinessException;
import java.util.Map;

public class OAuthUserInfoFactory {
    public static OAuthUserInfo create(String provider, Map<String, Object> attributes) {
        return switch (provider) {
            case "google" -> new GoogleUserInfo(attributes);
            case "kakao" -> new KakaoUserInfo(attributes);
            case "naver" -> new NaverUserInfo(attributes);
            default -> throw new BusinessException(ErrorCode.UNSUPPORTED_SOCIAL_PROVIDER);
        };
    }
}
