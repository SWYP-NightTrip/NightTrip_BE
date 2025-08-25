package com.nighttrip.core.global.ai.header;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class ClovaHeaders {
    @Value("${ncp.clova.apiKey}") private String apiKey;

    public Consumer<HttpHeaders> apply() {
        return h -> {
            h.setBearerAuth(apiKey); // Authorization: Bearer nv-...
            h.set(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8");
            // 필요 시 요청마다 트래킹용 ID 부여
            h.set("X-NCP-CLOVASTUDIO-REQUEST-ID", java.util.UUID.randomUUID().toString().replace("-", ""));
        };
    }
}