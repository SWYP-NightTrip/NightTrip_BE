package com.nighttrip.core.ai.dto;

import java.util.Map;

public record RerankCandidate(
        Long id,
        String spotName,
        String category,              // SpotCategory.name() 또는 한글명
        double popularity,            // 0~1 (normPopularity(mainWeight, checkCount))
        Map<String, Object> meta      // computedMeta 파싱(Map)
) {}