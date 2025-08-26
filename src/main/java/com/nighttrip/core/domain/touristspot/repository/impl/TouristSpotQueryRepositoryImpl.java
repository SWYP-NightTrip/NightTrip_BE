package com.nighttrip.core.domain.touristspot.repository.impl;

import com.nighttrip.core.domain.touristspot.entity.QTouristSpot;
import com.nighttrip.core.domain.touristspot.repository.TouristSpotQueryRepository;
import com.nighttrip.core.global.ai.dto.CandidateDto;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TouristSpotQueryRepositoryImpl implements TouristSpotQueryRepository {

    private static final QTouristSpot ts = QTouristSpot.touristSpot;
    private final JPAQueryFactory queryFactory;

    /**
     * Haversine 거리(km) QueryDSL 표현식
     */
    private static NumberExpression<Double> haversineKm(
            NumberExpression<Double> latCol, NumberExpression<Double> lngCol,
            double centerLat, double centerLng
    ) {
        // 6371 * 2 * asin( sqrt( sin^2((lat - :lat)/2) + cos(:lat)*cos(lat) * sin^2((lng - :lng)/2) ) )
        return Expressions.numberTemplate(Double.class,
                "6371 * 2 * asin( sqrt( power(sin(radians(( {0} - {1} )/2)),2) " +
                "+ cos(radians({1})) * cos(radians({0})) " +
                "* power(sin(radians(( {2} - {3} )/2)),2) ) ) )",
                latCol, centerLat, lngCol, centerLng);
    }

    @Override
    public List<CandidateDto> findCandidates(Long cityId, int limit, int offset) {
        return queryFactory
                .select(Projections.constructor(
                        CandidateDto.class,
                        ts.id,
                        ts.spotName,
                        ts.category,
                        ts.mainWeight,
                        ts.checkCount,
                        ts.computedMeta
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
