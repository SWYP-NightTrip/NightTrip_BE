package com.nighttrip.core.oauth.service;

import com.nighttrip.core.global.enums.ErrorCode;
import com.nighttrip.core.global.enums.Oauth_Provider;
import com.nighttrip.core.global.exception.BusinessException;
import com.nighttrip.core.oauth.dto.OAuthUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KakaoOAuthService implements OAuthLoginService {

    private final WebClient webClient;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;

    @Override
    public OAuthUserInfo getUserInfo(String code) {
        String decodedCode = URLDecoder.decode(code, StandardCharsets.UTF_8);

        String accessToken = webClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                        .with("client_id", clientId)
                        .with("redirect_uri", redirectUri)
                        .with("code", decodedCode))
                .retrieve()
                .onStatus(status -> status.is4xxClientError(),
                        response -> Mono.error(new BusinessException(ErrorCode.INVALID_OAUTH_CODE_INVALID)))
                .onStatus(status -> status.is5xxServerError(),
                        response -> Mono.error(new BusinessException(ErrorCode.OAUTH_PROVIDER_ERROR)))
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .map(body -> {
                    if (!body.containsKey("access_token")) {
                        throw new BusinessException(ErrorCode.INVALID_OAUTH_CODE_INVALID);
                    }
                    return (String) body.get("access_token");
                })
                .block();

        Map<String, Object> userInfo = webClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .headers(headers -> {
                    headers.setBearerAuth(accessToken);
                    headers.set("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
                })
                .retrieve()
                .onStatus(status -> status.is4xxClientError(),
                        response -> Mono.error(new BusinessException(ErrorCode.INVALID_OAUTH_CODE_INVALID)))
                .onStatus(status -> status.is5xxServerError(),
                        response -> Mono.error(new BusinessException(ErrorCode.OAUTH_PROVIDER_ERROR)))
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        if (userInfo == null || userInfo.get("id") == null) {
            throw new BusinessException(ErrorCode.INVALID_OAUTH_CODE_INVALID);
        }

        Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        return new OAuthUserInfo(
                (String) kakaoAccount.get("email"),
                (String) profile.get("nickname"),
                userInfo.get("id").toString(),
                Oauth_Provider.KAKAO
        );
    }
}
