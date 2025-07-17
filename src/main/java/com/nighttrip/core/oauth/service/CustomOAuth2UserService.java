package com.nighttrip.core.oauth.service;

import com.nighttrip.core.domain.user.entity.User;
import com.nighttrip.core.domain.user.repository.UserRepository;
import com.nighttrip.core.global.enums.ErrorCode;
import com.nighttrip.core.global.exception.BusinessException;
import com.nighttrip.core.oauth.userinfo.OAuthUserInfoFactory;
import com.nighttrip.core.oauth.userinfo.OAuthUserInfo;
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
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

        User user = userRepository.findByEmail(userInfo.getEmail())
                .map(existingUser -> {
                    if (existingUser.getProvider() != userInfo.getProvider()) {
                        throw new BusinessException(ErrorCode.EMAIL_ALREADY_REGISTERED);
                    }
                    return existingUser;
                })
                .orElseGet(() -> userRepository.save(new User(
                        userInfo.getEmail(),
                        userInfo.getNickname(),
                        userInfo.getSocialId(),
                        userInfo.getProvider()
                )));



        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().name())),
                attributes,
                userNameAttributeName
        );
    }
}
