package com.nighttrip.core.global.maps;

import com.fasterxml.jackson.databind.JsonNode;
import com.nighttrip.core.global.enums.ErrorCode;
import com.nighttrip.core.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class NaverMapFunction {

    private final RestTemplate restTemplate;

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    public GeocodeResponse geocode(String address) {
        String url = UriComponentsBuilder
                .fromHttpUrl("https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode")
                .queryParam("query", address)
                .build()
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-NCP-APIGW-API-KEY-ID", clientId);
        headers.set("X-NCP-APIGW-API-KEY", clientSecret);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                JsonNode.class
        );

        JsonNode body = response.getBody();

        if(!body.get("status").asText().equals("OK")){
            throw new BusinessException(ErrorCode.GEOCODE_FAILED);
        }

        JsonNode addressNode = body.get("addresses");
        if (addressNode == null || !addressNode.isArray() || addressNode.isEmpty()) {
            throw new BusinessException(ErrorCode.GEOCODE_FAILED);
        }

        JsonNode first = addressNode.get(0);
        String x = first.get("x").asText();
        String y = first.get("y").asText();

        return new GeocodeResponse(x, y);
    }

}
