package com.nighttrip.core.global.ai.dto;

import java.util.List;

public record UserContext(
        String timeMode,             // "night"|"day"
        List<String> travelStyle,    // ["감성","느긋한"]
        String companions,           // "커플"|"가족"|"친구"|"혼자"
        List<String> preferences,    // ["야경","맛집"]
        int maxSpots,
        double centerLat, double centerLng
) {}
