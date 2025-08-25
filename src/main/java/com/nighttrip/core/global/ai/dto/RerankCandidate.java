package com.nighttrip.core.global.ai.dto;

import java.util.Map;

public record RerankCandidate(
        Long id, String spotName, String category,
        double distKm, double popularityHint,
        Map<String, Object> meta // 라벨링 JSON 파싱본
) {}
