package com.nighttrip.core.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nighttrip.core.global.dto.ApiErrorResponse;
import com.nighttrip.core.oauth.service.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer; // Customizer 임포트 확인
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration; // CorsConfiguration 임포트
import org.springframework.web.cors.CorsConfigurationSource; // CorsConfigurationSource 임포트
import org.springframework.web.cors.UrlBasedCorsConfigurationSource; // UrlBasedCorsConfigurationSource 임포트

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final ObjectMapper objectMapper;
    @Value("${frontend.url}")
    private String frontUrl;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // CSRF는 일단 비활성화하여 테스트 (운영 시에는 JWT나 CSRF 토큰 방식 고려)
                .cors(Customizer.withDefaults()) // <--- 이 부분 추가 (CORS 설정 활성화)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("index.html", "/", "/css/**", "/images/**", "/js/**", "/favicon.ico").permitAll()
                        .requestMatchers("/oauth2/**", "/login/**", "/api/health-check", "/api/v1/oauth/status").permitAll()
                        .requestMatchers("/api/v1/search/**").permitAll()
                        .requestMatchers("/api/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        ).defaultSuccessUrl(frontUrl + "/", true)
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((request, response, authException) -> {
                            log.warn("Unauthorized Access Attempt: URI={} - Message={}", request.getRequestURI(), authException.getMessage());
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

                            objectMapper.writeValue(response.getWriter(), ApiErrorResponse.of(
                                    HttpStatus.UNAUTHORIZED.name(),
                                    "인증되지 않은 요청입니다. 로그인이 필요합니다."
                            ));
                        })
                );

        return http.build();
    }

    // WebConfig의 CORS 설정을 이 SecurityConfig로 옮겨옵니다.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // allowedOrigins에 프론트엔드 URL과 개발 백엔드 URL을 모두 명시합니다.
        configuration.addAllowedOrigin("https://localhost:3000"); // 로컬 개발 프론트엔드 URL
        configuration.addAllowedOrigin("https://www.nighttrip.co.kr"); // 실제 운영/배포 환경 프론트엔드 URL
        configuration.addAllowedOrigin("https://dev.nighttrip.co.kr"); // 개발 백엔드 URL (테스트용, 필요시 추가)

        configuration.addAllowedMethod("*"); // 모든 HTTP 메서드 허용
        configuration.addAllowedHeader("*"); // 모든 헤더 허용
        configuration.setAllowCredentials(true); // 중요: 자격 증명(쿠키, 인증 헤더) 전송 허용
        configuration.setMaxAge(3600L); // Pre-flight 요청 캐시 시간

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 경로에 CORS 적용
        return source;
    }
}