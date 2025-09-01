package com.nighttrip.core.ai.dto;

import com.nighttrip.core.global.enums.SpotCategory;

public record CandidateDto(
        Long id,
        String spotName,
        SpotCategory category,
        Integer mainWeight,
        Integer subWeight,
        Integer checkCount,
        String computedMeta,
        Double latitude,
        Double longitude
) {}