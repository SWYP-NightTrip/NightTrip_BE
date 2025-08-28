package com.nighttrip.core.ai.dto;

import java.util.List;

public record LabeledMeta(
        Long id,
        List<String> tags,
        Double night_suitability,
        List<String> style,
        List<String> companions_fit,
        String dwell_time_min,
        List<String> pros,
        List<String> cons,
        List<String> must_know,
        List<String> evidence
) {}