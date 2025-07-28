package com.nighttrip.core.domain.touristspot.repository;

import com.nighttrip.core.domain.touristspot.dto.TouristSpotPopularityDto;
import com.nighttrip.core.domain.touristspot.entity.TouristSpot;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface TouristSpotRepository extends JpaRepository<TouristSpot, Long> {

    @Query("""
                select max(t.orderIndex)
                from TripOrder t
                where t.tripDay.tripPlan.id=:tripPlanId
                and t.tripDay.dayOrder=:tripDayOrder
            """)
    Optional<BigDecimal> findLastOrder(@Param("tripPlanId") Long tripPlanId, @Param("tripDayOrder") Integer tripDayOrder);

    @Query("SELECT ts FROM TouristSpot ts " +
            "LEFT JOIN ts.tourLikes tl " +
            "LEFT JOIN ts.touristSpotReviews tsr " +
            "LEFT JOIN ts.bookMarks bm " +
            "WHERE ts.city.id = :cityId " +
            "GROUP BY ts.id, ts.spotName, ts.address, ts.category, ts.spotDescription, " +
            "ts.longitude, ts.latitude, ts.checkCount, ts.link, ts.telephone, " +
            "ts.mainWeight, ts.subWeight, ts.city, ts.tripOrder " +
            "ORDER BY (COALESCE(COUNT(DISTINCT tl.id), 0) + " +
            "COALESCE(COUNT(DISTINCT tsr.reviewId), 0) + " +
            "COALESCE(COUNT(DISTINCT bm.id), 0)) DESC")
    List<TouristSpot> findPopularTouristSpotsByCityId(@Param("cityId") Long cityId, Pageable pageable);


    @Query("SELECT ts FROM TouristSpot ts " +
            "LEFT JOIN FETCH ts.touristSpotImageUris ti " +
            "WHERE ts.city.id = :cityId " +
            "ORDER BY (COALESCE(ts.checkCount, 0) + COALESCE(ts.mainWeight, 0) + COALESCE(ts.subWeight, 0)) DESC, " +
            "ts.category ASC")
    List<TouristSpot> findRecommendedTouristSpotsByCityId(@Param("cityId") Long cityId, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"city", "touristSpotImageUris"}) // ★★★ 이 부분을 추가 또는 확인! ★★★
    List<TouristSpot> findAll();

}
