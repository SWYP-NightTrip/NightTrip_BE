package com.nighttrip.core.global.ai.dto;

import java.util.List;

public record ClovaRequest(
        List<ClovaMessage> messages,
        Double top,
        Double temperature,
        Integer maxTokens
) {
    // 기본 생성자 (기본값 설정 포함)
    public ClovaRequest() {
        this(List.of(), 0.8, 0.7, 500);
    }

    // messages만 받아서 기본값 설정하는 보조 생성자
    public ClovaRequest(List<ClovaMessage> messages) {
        this(messages, 0.8, 0.7, 500);
    }
}
