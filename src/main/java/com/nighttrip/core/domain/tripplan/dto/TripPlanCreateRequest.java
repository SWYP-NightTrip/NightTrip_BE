package com.nighttrip.core.domain.tripplan.dto;

import java.time.LocalDate;
import java.util.List;

public record TripPlanCreateRequest(
        String title,
        LocalDate startDate,
        LocalDate endDate,
        List<String> cities
) {}