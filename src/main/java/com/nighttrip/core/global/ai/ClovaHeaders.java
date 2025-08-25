package com.nighttrip.core.global.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class ClovaHeaders {
    @Value("${ncp.clova.apiKey}") private String apiKey;
    @Value("${ncp.clova.apigwKey}") private String apigwKey;
    @Value("${ncp.clova.userId}") private String userId;

    public Consumer<HttpHeaders> apply() {
        return headers -> {
            headers.set("X-NCP-CLOVASTUDIO-API-KEY", apiKey);
            headers.set("X-NCP-APIGW-API-KEY", apigwKey);
            headers.set("X-NCP-USER-ID", userId);
        };
    }
}
