package com.nighttrip.core.domain.touristspot.repository.impl;

import com.nighttrip.core.ai.dto.CandidateDto;
import com.nighttrip.core.domain.touristspot.entity.QTouristSpot;
import com.nighttrip.core.domain.touristspot.repository.TouristSpotQueryRepository;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TouristSpotQueryRepositoryImpl implements TouristSpotQueryRepository {

    private static final QTouristSpot ts = QTouristSpot.touristSpot;
    private final JPAQueryFactory queryFactory;

    @Override
    public List<CandidateDto> findCandidates(Long cityId, int limit, int offset) {
        return queryFactory
                .select(Projections.constructor(
                        CandidateDto.class,
                        ts.id,
                        ts.spotName,
                        ts.category,
                        ts.mainWeight,
                        ts.subWeight,
                        ts.checkCount,
                        ts.computedMeta,
                        ts.latitude,
                        ts.longitude
                ))
                .from(ts)
                .where(ts.city.id.eq(cityId))
                .orderBy(
                        ts.mainWeight.coalesce(0).desc(),
                        ts.checkCount.coalesce(0).desc(),
                        ts.subWeight.coalesce(0).desc(),
                        ts.id.asc()
                )
                .limit(limit)
                .offset(offset)
                .fetch();
    }
}
