package com.nighttrip.core.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nighttrip.core.global.dto.ApiErrorResponse;
import com.nighttrip.core.oauth.service.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/css/**", "/images/**", "/js/**", "/favicon.ico").permitAll()
                        .requestMatchers("/oauth2/**", "/login/**", "/api/health-check", "/api/v1/oauth/status").permitAll()
                        .requestMatchers("/api/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .defaultSuccessUrl("/api/v1/main", true)
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
}