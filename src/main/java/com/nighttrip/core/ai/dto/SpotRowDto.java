package com.nighttrip.core.ai.dto;

import com.nighttrip.core.global.enums.SpotCategory;

public record SpotRowDto(
        Long id,
        String spotName,
        String address,
        SpotCategory category,
        long reviewCount,
        double avgScope,
        String thumbUrl
) {}