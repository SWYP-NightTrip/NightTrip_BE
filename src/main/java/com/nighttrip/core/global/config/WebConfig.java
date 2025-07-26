package com.nighttrip.core.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")   // 모든 경로에 대해 CORS 설정 적용
                .allowedOrigins("http://localhost:3000", "https://dev.nighttrip.co.kr")  // 허용할 출처(도메인)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // 허용할 HTTP 메서드
                .allowCredentials(true);  // 쿠키, 인증 정보(자격 증명)를 허용
    }
}
