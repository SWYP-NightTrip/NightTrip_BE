package com.nighttrip.core.global.config;
// 필요한 import 문들

import com.nighttrip.core.oauth.service.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.Collection; // Collection 임포트 추가

@Slf4j // Lombok 로거
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    @Value("${frontend.url}")
    private String frontUrl; // application.yaml 또는 환경변수에서 주입

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화 (Rest API에서 토큰 방식 사용 시)
                .cors(Customizer.withDefaults()) // CORS 설정 적용
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // 세션 사용
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/v1/oauth/status").permitAll() // 로그인 상태 확인 API는 인증 없이 접근 허용
                        .requestMatchers("/api/v1/search/**").permitAll() // 검색 API는 인증 없이 접근 허용
                        .requestMatchers("/oauth2/**").permitAll() // OAuth2 관련 경로는 인증 없이 접근 허용
                        .requestMatchers(HttpMethod.GET, "/health").permitAll() // 헬스 체크 경로 허용
                        .anyRequest().authenticated() // 그 외 모든 요청은 인증 필요
                )
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService) // 사용자 정보 가져오는 서비스 설정
                        )
                        // 로그인 성공 시 리다이렉션 될 URL 설정
                        // alwaysUse=true는 항상 이 URL로 리다이렉션하도록 강제
                        // 이제 frontUrl은 https://localhost:3000 이 될 것입니다.
                        .defaultSuccessUrl(frontUrl + "/", true)
                        // !!! 로그인 성공 핸들러 추가 !!!
                        .successHandler((request, response, authentication) -> {
                            log.info(">>>> OAuth2 Login Success Handler Called!");
                            log.info(">>>> Redirecting to: {}", frontUrl + "/");

                            // 응답 헤더에서 "Set-Cookie" 헤더를 모두 가져와 로깅
                            Collection<String> setCookieHeaders = response.getHeaders("Set-Cookie");
                            if (setCookieHeaders != null && !setCookieHeaders.isEmpty()) {
                                log.info(">>>> Found Set-Cookie Headers (Count: {}):", setCookieHeaders.size());
                                for (String cookie : setCookieHeaders) {
                                    log.info(">>>> Set-Cookie Header: {}", cookie);
                                }
                            } else {
                                log.warn(">>>> No 'Set-Cookie' headers found after OAuth2 login success!");
                            }

                            // 원래의 리다이렉트 로직을 수동으로 호출
                            response.sendRedirect(frontUrl + "/");
                        })
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login") // 로그아웃 성공 시 리다이렉션 될 URL
                        .permitAll() // 로그아웃 경로는 인증 없이 접근 허용
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // CORS 허용할 Origin 목록. 이제 프론트엔드도 HTTPS이므로 추가해야 합니다.
        configuration.addAllowedOrigin("https://localhost:3000"); // 로컬 개발용 프론트엔드 HTTPS URL
        configuration.addAllowedOrigin("https://www.nighttrip.co.kr"); // 운영 환경 프론트엔드 URL
        configuration.addAllowedOrigin("https://dev.nighttrip.co.kr"); // 백엔드 개발 서버 URL (프론트엔드로 사용될 수도 있음)

        configuration.addAllowedMethod("*"); // 모든 HTTP 메서드 허용 (GET, POST, PUT, DELETE 등)
        configuration.addAllowedHeader("*"); // 모든 헤더 허용
        configuration.setAllowCredentials(true); // 인증 정보(쿠키, HTTP 인증 헤더 등) 전송 허용
        configuration.setMaxAge(3600L); // Preflight 요청 결과 캐싱 시간

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 경로에 CORS 설정 적용
        return source;
    }
}