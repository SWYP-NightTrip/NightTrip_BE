package com.nighttrip.core.global.config;

import com.nighttrip.core.oauth.service.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession; // HttpSession 임포트 유지
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseCookie; // ResponseCookie 임포트 유지
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
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    @Value("${frontend.url}")
    private String frontUrl;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation().changeSessionId()
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/v1/oauth/status").permitAll()
                        .requestMatchers("/api/v1/search/**", "/api/v1/search/recommend", "/api/v1/search/popular").permitAll()
                        .requestMatchers("/oauth2/**").permitAll()
                        .requestMatchers("/favicon.ico").permitAll()
                        .requestMatchers(HttpMethod.GET, "/health").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler((request, response, authentication) -> {
                            log.info(">>>> OAuth2 로그인 성공 핸들러 호출됨!");
                            log.info(">>>> {}로 리다이렉트합니다.", frontUrl + "/");

                            HttpSession session = request.getSession(false); // 세션이 없다면 생성하지 않음
                            String sessionId = null;
                            if (session != null) {
                                sessionId = session.getId();
                            }

                            if (sessionId != null) {
                                ResponseCookie jsessionidCookie = ResponseCookie.from("JSESSIONID", sessionId)
                                        .path("/")
                                        .httpOnly(true)    // HttpOnly: true
                                        .secure(true)      // Secure: true
                                        .sameSite("None")  // ← 변경: SameSite=None
                                        .domain("dev.nighttrip.co.kr") // 현재 백엔드 도메인으로 정확히 명시
                                        .maxAge(Duration.ofDays(7)) // 7일 유지
                                        .build();
                                response.addHeader("Set-Cookie", jsessionidCookie.toString());
                                log.info(">>>> JSESSIONID 쿠키 헤더를 수동으로 추가했습니다: {}", jsessionidCookie.toString());
                            } else {
                                log.warn(">>>> OAuth2 로그인 후 JSESSIONID가 null입니다. 수동으로 쿠키를 추가할 수 없습니다.");
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

        configuration.setAllowCredentials(true);

        configuration.setAllowedOrigins(Arrays.asList(
                "https://localhost:3000",
                "http://localhost:3000",
                "https://www.nighttrip.co.kr",
                "https://dev.nighttrip.co.kr"
        ));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));

        configuration.setExposedHeaders(Arrays.asList("Set-Cookie", "Authorization", "accessToken", "refreshToken"));

        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}