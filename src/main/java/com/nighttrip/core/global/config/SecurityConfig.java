package com.nighttrip.core.global.config;

import com.nighttrip.core.oauth.service.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseCookie; // ResponseCookie 임포트 추가
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
// Collection 임포트가 더 이상 필요 없을 수 있지만, 혹시 몰라 남겨둡니다.
// import java.util.Collection;

@Slf4j // Lombok 로거
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    @Value("${frontend.url}")
    private String frontUrl; // application.yaml 또는 환경변수에서 주입

    // 백엔드 쿠키 도메인을 application.yaml에서 가져오도록 설정
    @Value("${spring.session.servlet.cookie.domain}")
    private String cookieDomain;

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
                        .requestMatchers("/favicon.ico").permitAll() // favicon.ico 요청 허용
                        .requestMatchers(HttpMethod.GET, "/health").permitAll() // 헬스 체크 경로 허용
                        .anyRequest().authenticated() // 그 외 모든 요청은 인증 필요
                )
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService) // 사용자 정보 가져오는 서비스 설정
                        )
                        // defaultSuccessUrl은 이제 successHandler에서 처리하므로, 여기서는 기본 리다이렉션만 설정
                        // successHandler가 항상 호출되므로 defaultSuccessUrl은 내부적으로 사용되지 않습니다.
                        .defaultSuccessUrl(frontUrl + "/", true)
                        // !!! 로그인 성공 핸들러 추가 !!!
                        .successHandler((request, response, authentication) -> {
                            log.info(">>>> OAuth2 Login Success Handler Called!");
                            log.info(">>>> Redirecting to: {}", frontUrl + "/"); // https://localhost:3000/

                            // Spring Security가 이미 생성한 JSESSIONID 세션 ID를 가져옵니다.
                            // 세션이 존재하지 않으면 (매우 드물겠지만) NPE 방지를 위해 null 체크
                            String sessionId = null;
                            if (request.getSession(false) != null) {
                                sessionId = request.getSession(false).getId();
                            }

                            if (sessionId != null) {
                                // JSESSIONID 쿠키를 직접 생성하여 응답에 추가
                                // 이전에 email/provider 쿠키를 성공적으로 보냈던 속성들을 사용합니다.
                                ResponseCookie jsessionidCookie = ResponseCookie.from("JSESSIONID", sessionId)
                                        .path("/") // 모든 경로에서 유효
                                        .domain(cookieDomain) // application-local.yaml의 dev.nighttrip.co.kr 도메인 사용
                                        .secure(false) // HTTPS 통신이지만, localhost와의 호환성을 위해 false로 설정
                                        .httpOnly(true) // JavaScript 접근 방지
                                        .sameSite("Lax") // 탑-레벨 내비게이션에서 쿠키 전송 허용
                                        .maxAge(60 * 60 * 24 * 7) // 예: 7일 유효
                                        .build();

                                response.addHeader("Set-Cookie", jsessionidCookie.toString());
                                log.info(">>>> Manually added Set-Cookie Header for JSESSIONID: {}", jsessionidCookie.toString());
                            } else {
                                log.warn(">>>> JSESSIONID was null after OAuth2 login. Cannot manually add cookie.");
                            }

                            // 최종 프론트엔드로 리다이렉트
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
        // CORS 허용할 Origin 목록
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
