package com.nighttrip.core.global.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.nighttrip.core.global.ai.dto.ClovaMessage;
import com.nighttrip.core.global.ai.dto.ClovaRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClovaService {

    @Value("${naver.client-id}")
    private String apiKey;
    @Value("${naver.client-secret}")
    private String gatewayKey;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://clovastudio.stream.ntruss.com/testapp/v1")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();

    public String getRecommendation(String systemPrompt, String userPrompt) {
        List<ClovaMessage> messages = List.of(
                new ClovaMessage("system", systemPrompt),
                new ClovaMessage("user", userPrompt)
        );

        ClovaRequest request = new ClovaRequest(messages);

        return webClient.post()
                .uri("/chat-completions/HCX-005")
                .header("X-NCP-CLOVASTUDIO-API-KEY", apiKey)
                .header("X-NCP-APIGW-API-KEY", gatewayKey)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(json -> json.path("result").path("message").path("content").asText())
                .block();
    }

}
