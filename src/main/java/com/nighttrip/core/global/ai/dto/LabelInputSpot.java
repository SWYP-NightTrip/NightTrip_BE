package com.nighttrip.core.global.ai.dto;

import java.util.List;

public record LabelInputSpot(
        Long id, String spotName, String category,
        String address, Double lat, Double lng,
        String spotDescription, List<String> touristSpotDetails  ) {}
