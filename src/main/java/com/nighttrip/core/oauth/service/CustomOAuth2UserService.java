package com.nighttrip.core.oauth.service;

import com.nighttrip.core.domain.user.entity.User;
import com.nighttrip.core.domain.user.repository.UserRepository;
import com.nighttrip.core.global.enums.ErrorCode;
import com.nighttrip.core.global.exception.BusinessException;
import com.nighttrip.core.oauth.userinfo.OAuthUserInfoFactory;
import com.nighttrip.core.oauth.userinfo.OAuthUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 이 라인 추가
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Optional; // Optional 임포트 추가

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        Map<String, Object> attributes = oauth2User.getAttributes();

        OAuthUserInfo userInfo = OAuthUserInfoFactory.create(registrationId, attributes);

        if (userInfo.getEmail() == null || userInfo.getNickname() == null || userInfo.getSocialId() == null) {
            throw new BusinessException(ErrorCode.MISSING_SOCIAL_INFO);
        }

        User user;
        Optional<User> existingUserOptional = userRepository.findByEmail(userInfo.getEmail());

        if (existingUserOptional.isPresent()) {
            User existingUser = existingUserOptional.get();

            if (existingUser.getProvider() != userInfo.getProvider()) {
                throw new BusinessException(ErrorCode.EMAIL_ALREADY_REGISTERED);
            }
            user = existingUser;
        } else {
            User newUser = new User(
                    userInfo.getEmail(),
                    userInfo.getNickname(),
                    userInfo.getSocialId(),
                    userInfo.getProvider()
            );

            try {
                user = userRepository.save(newUser);
            } catch (Exception e) {
                throw new RuntimeException("OAuth 프로세스 중 사용자 저장 실패", e);
            }
        }

        log.info("사용자 {}의 역할: {}로 DefaultOAuth2User 반환.", user.getEmail(), user.getRole().name());
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().name())),
                attributes,
                userNameAttributeName
        );
    }
}