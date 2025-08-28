package com.nighttrip.core.ai.dto;

import java.util.Map;

public record RerankCandidate(
        Long id,
        String spotName,
        String category,
        double popularityHint,
        Map<String, Object> meta
) {}