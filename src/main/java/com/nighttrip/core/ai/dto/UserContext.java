package com.nighttrip.core.ai.dto;

import jakarta.validation.constraints.Max;

import java.util.List;

public record UserContext(
        String timeMode,           // "night" | "day"
        List<String> travelStyle,  // ["감성","느긋한"]
        String companions,         // "커플"|"가족"|"친구"|"혼자"
        List<String> preferences,  // ["야경","맛집"]

        @Max(value = 10)
        Integer maxSpots           // null 또는 0/음수면 무시
) {}