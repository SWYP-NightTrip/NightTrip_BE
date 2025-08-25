package com.nighttrip.core.global.ai.dto;

public record CandidateDto(
        Long id,
        String spotName,
        String category,
        Integer mainWeight,
        Integer checkCount,
        String computedMeta,
        double distKm
) {}