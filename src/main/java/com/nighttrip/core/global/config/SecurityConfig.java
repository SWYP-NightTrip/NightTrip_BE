package com.nighttrip.core.global.config;

import com.nighttrip.core.oauth.service.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession; // HttpSession 임포트 추가
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseCookie;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j // Lombok 로거
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    @Value("${frontend.url}")
    private String frontUrl; // application.yaml 또는 환경변수에서 주입 (예: https://localhost:3000)

    // 백엔드 쿠키 도메인을 application.yaml에서 가져오도록 설정
    @Value("${spring.session.servlet.cookie.domain}")
    private String cookieDomain; // 예: dev.nighttrip.co.kr

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
                        .requestMatchers("/api/v1/search/**","/api/v1/search/recommend","/api/v1/search/popular").permitAll() // 검색 API는 인증 없이 접근 허용
                        .requestMatchers("/oauth2/**").permitAll() // OAuth2 관련 경로는 인증 없이 접근 허용
                        .requestMatchers("/favicon.ico").permitAll() // favicon.ico 요청 허용
                        .requestMatchers(HttpMethod.GET, "/health").permitAll() // 헬스 체크 경로 허용
                        .anyRequest().authenticated() // 그 외 모든 요청은 인증 필요
                )
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService) // 사용자 정보 가져오는 서비스 설정
                        )

                        .defaultSuccessUrl(frontUrl + "/", true)
                        .successHandler((request, response, authentication) -> {
                            log.info(">>>> OAuth2 Login Success Handler Called!");
                            log.info(">>>> Redirecting to: {}", frontUrl + "/"); // https://localhost:3000/

                            HttpSession session = request.getSession(false);
                            String sessionId = null;
                            if (session != null) {
                                sessionId = session.getId();
                            }

                            if (sessionId != null) {
                                ResponseCookie jsessionidCookie = ResponseCookie.from("JSESSIONID", sessionId)
                                        .path("/") // 모든 경로에서 유효
                                        .domain(cookieDomain) // application.yaml의 dev.nighttrip.co.kr 도메인 사
                                        .httpOnly(true) // JavaScript 접근 방지
                                        .secure(true) // <--- 이 부분을 다시 true로 변경해봅니다.
                                        .sameSite("Lax") // 탑-레벨 내비게이션에서 쿠키 전송 허용
                                        .maxAge(60 * 60 * 24 * 7) // 예: 7일 유효
                                        .build();

                                response.addHeader("Set-Cookie", jsessionidCookie.toString());
                                log.info(">>>> Manually added Set-Cookie Header for JSESSIONID: {}", jsessionidCookie.toString());
                            } else {
                                log.warn(">>>> JSESSIONID was null after OAuth2 login. Cannot manually add cookie.");
                            }
                            response.sendRedirect(frontUrl + "/");
                        })
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true); // 인증 정보(쿠키, HTTP 인증 헤더 등) 전송 허용

        // CORS 허용할 Origin 목록
        configuration.setAllowedOrigins(Arrays.asList(
                "https://localhost:3000",
                "https://www.nighttrip.co.kr",
                "https://dev.nighttrip.co.kr"
                ));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Origin", "Content-Type", "Accept", "Set-Cookie","Access-Control-Allow-Origin"));
        configuration.setExposedHeaders(List.of("Set-Cookie","Access-Control-Allow-Origin"));

        configuration.setMaxAge(3600L); // Preflight 요청 결과 캐싱 시간

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 경로에 CORS 설정 적용
        return source;
    }
}
