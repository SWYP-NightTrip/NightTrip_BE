package com.nighttrip.core.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ClovaClientConfig {
    @Bean
    public WebClient clovaWebClient(@Value("${ncp.clova.endpoint}") String endpoint) {
        return WebClient.builder()
                .baseUrl(endpoint) // https://clovastudio.stream.ntruss.com
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                .build();
    }
}