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
@Slf4j // 이 어노테이션 추가
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // --- 1. OAuth2User 정보 로딩 시작 ---
        log.info("OAuth2 사용자 로딩 시작. 등록 ID: {}", userRequest.getClientRegistration().getRegistrationId());
        OAuth2User oauth2User = new DefaultOAuth2UserService().loadUser(userRequest);
        log.info("OAuth2 사용자 정보 로드 완료. 속성: {}", oauth2User.getAttributes());

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        log.info("등록 ID: {}, 사용자 이름 속성명: {}", registrationId, userNameAttributeName);

        Map<String, Object> attributes = oauth2User.getAttributes();
        // attributes 맵 전체를 로그에 출력하여 어떤 정보가 넘어오는지 확인 (디버그 레벨)
        log.debug("OAuth2 원본 속성 데이터: {}", attributes);

        // --- 2. OAuthUserInfo 생성 및 필수 정보 검증 ---
        OAuthUserInfo userInfo = OAuthUserInfoFactory.create(registrationId, attributes);
        log.info("OAuthUserInfo 생성 완료. 이메일: {}, 닉네임: {}, 소셜 ID: {}, 프로바이더: {}",
                userInfo.getEmail(), userInfo.getNickname(), userInfo.getSocialId(), userInfo.getProvider());


        if (userInfo.getEmail() == null || userInfo.getNickname() == null || userInfo.getSocialId() == null) {
            log.error("필수 소셜 정보 누락. 이메일: {}, 닉네임: {}, 소셜 ID: {}",
                    userInfo.getEmail(), userInfo.getNickname(), userInfo.getSocialId());
            throw new BusinessException(ErrorCode.MISSING_SOCIAL_INFO);
        }

        // --- 3. 기존 사용자 조회 또는 신규 사용자 저장 로직 ---
        User user;
        Optional<User> existingUserOptional = userRepository.findByEmail(userInfo.getEmail());

        if (existingUserOptional.isPresent()) {
            User existingUser = existingUserOptional.get();
            log.info("기존 사용자 발견. 이메일: {}. 프로바이더: {}", existingUser.getEmail(), existingUser.getProvider());

            if (existingUser.getProvider() != userInfo.getProvider()) {
                log.error("이메일 {}은(는) 다른 프로바이더로 이미 등록됨. 기존: {}, 신규: {}",
                        userInfo.getEmail(), existingUser.getProvider(), userInfo.getProvider());
                throw new BusinessException(ErrorCode.EMAIL_ALREADY_REGISTERED);
            }
            user = existingUser;
            log.info("사용자 {} (ID: {})가 OAuth를 통해 로그인했습니다 (기존 사용자).", user.getEmail(), user.getId());
        } else {
            // 새로운 User 객체 생성 전 로그
            log.info("이메일 {}을(를) 가진 기존 사용자를 찾을 수 없습니다. 새로운 사용자를 생성합니다.", userInfo.getEmail());
            User newUser = new User(
                    userInfo.getEmail(),
                    userInfo.getNickname(),
                    userInfo.getSocialId(),
                    userInfo.getProvider()
            );
            log.info("새 사용자 저장 시도 중: 이메일={}, 닉네임={}, 소셜 ID={}, 프로바이더={}",
                    newUser.getEmail(), newUser.getNickname(), newUser.getSocialId(), newUser.getProvider());

            try {
                user = userRepository.save(newUser);
                log.info("새 사용자 저장 성공! 사용자 ID: {}, 이메일: {}", user.getId(), user.getEmail());
            } catch (Exception e) {
                log.error("새 사용자 {} 저장 실패. 오류: {}", newUser.getEmail(), e.getMessage(), e);
                // 저장 실패 시 추가적인 예외 처리 또는 재시도 로직 고려
                throw new RuntimeException("OAuth 프로세스 중 사용자 저장 실패", e); // 더 명확한 예외로 변경 가능
            }
        }

        // --- 4. DefaultOAuth2User 반환 ---
        log.info("사용자 {}의 역할: {}로 DefaultOAuth2User 반환.", user.getEmail(), user.getRole().name());
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().name())),
                attributes,
                userNameAttributeName
        );
    }
}