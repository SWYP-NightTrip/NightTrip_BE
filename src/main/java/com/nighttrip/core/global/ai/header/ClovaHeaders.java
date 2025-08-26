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
        return h -> {
            String k = apiKey == null ? "" : apiKey.trim();
            // 키 비었으면 즉시 경고(이 상태로 호출하면 401)
            if (k.isEmpty()) {
                log.error("[CLOVA] apiKey is EMPTY. Authorization header will be invalid and cause 401.");
            }
            h.setBearerAuth(k); // Authorization: Bearer nv-...
            h.set(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");

            String reqId = UUID.randomUUID().toString().replace("-", "");
            h.set("X-NCP-CLOVASTUDIO-REQUEST-ID", reqId);

            // 요청마다 헤더 상태 로그(민감정보 마스킹)
            log.debug("[CLOVA] Set headers -> Authorization='{}', Content-Type='{}', REQUEST-ID={}",
                    (k.isEmpty() ? "EMPTY" : ("Bearer " + mask(k))),
                    "application/json; charset=UTF-8",
                    reqId);
        };
    }
}