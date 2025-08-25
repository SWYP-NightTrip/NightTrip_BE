package com.nighttrip.core.domain.touristspot.repository;

import com.nighttrip.core.global.ai.dto.CandidateDto;

import java.util.List;

public interface TouristSpotQueryRepository {
    List<CandidateDto> findCandidates(
            Long cityId, double centerLat, double centerLng,
            double radiusKm, int limit, int offset
    );
}