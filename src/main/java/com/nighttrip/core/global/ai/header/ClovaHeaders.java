package com.nighttrip.core.global.ai.header;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClovaHeaders {
    @Value("${ncp.clova.apiKey}") private String apiKey;

    private static String mask(String key) {
        if (key == null) return "null";
        String k = key.trim();
        if (k.length() <= 6) return "***";
        return k.substring(0, 3) + "****" + k.substring(k.length()-3);
    }

    @PostConstruct
    void logKeyOnBoot() {
        String k = apiKey == null ? null : apiKey.trim();
        log.info("[CLOVA] apiKey loaded? len={}, startsWith 'nv-'? {}, sample={}",
                (k == null ? null : k.length()),
                (k != null && k.startsWith("nv-")),
                mask(k));
    }

    public Consumer<HttpHeaders> apply() {
        // 애플리케이션 구동 후 첫 사용 때 경고/실패
        if (apiKey == null || apiKey.isBlank() || !apiKey.startsWith("nv-")) {
            log.error("[CLOVA] Invalid API key. Check property 'ncp.clova.apiKey' or env 'NCP_CLOVA_APIKEY'. current='{}'",
                    mask(apiKey));
            throw new IllegalStateException("CLOVA API key missing or invalid");
        }
        log.debug("[CLOVA] Using API key {}", mask(apiKey));

        return h -> {
            h.setBearerAuth(apiKey); // Authorization: Bearer nv-...
            h.set(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8");
            h.set("X-NCP-CLOVASTUDIO-REQUEST-ID", java.util.UUID.randomUUID().toString().replace("-", ""));
        };
    }

}