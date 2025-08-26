package com.nighttrip.core.global.ai.dto;

import com.nighttrip.core.global.enums.SpotCategory;

public record CandidateDto(
        Long id,
        String spotName,
        SpotCategory category,
        Integer mainWeight,
        Integer checkCount,
        String computedMeta
) {}