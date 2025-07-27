package com.nighttrip.core.domain.touristspot.repository;

import com.nighttrip.core.domain.touristspot.dto.TouristSpotPopularityDto;
import com.nighttrip.core.domain.touristspot.dto.TouristSpotWithDistance;
import com.nighttrip.core.domain.touristspot.entity.TouristSpot;
import org.springframework.data.domain.Pageable;
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

    /**
     * 2-a. 여행계획이 없고, 위치 정보가 있는 경우
     */
    @Query(value =
            "SELECT ts.*, " +
                    " (6371 * acos(cos(radians(:userLat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(latitude)))) AS distance " +
                    "FROM tourist_spot ts " +
                    "LEFT JOIN (SELECT tourist_spot_id, AVG(scope) as avg_scope FROM tourist_spot_review GROUP BY tourist_spot_id) rs ON ts.tourist_spot_id = rs.tourist_spot_id " +
                    "WHERE (6371 * acos(cos(radians(:userLat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(latitude)))) <= 70 " +
                    "ORDER BY " +
                    " (CASE " +
                    "     WHEN (6371 * acos(cos(radians(:userLat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(latitude)))) <= 5 THEN 100 " +
                    "     WHEN (6371 * acos(cos(radians(:userLat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(latitude)))) <= 15 THEN 80 " +
                    "     WHEN (6371 * acos(cos(radians(:userLat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(latitude)))) <= 30 THEN 50 " +
                    "     WHEN (6371 * acos(cos(radians(:userLat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(latitude)))) <= 60 THEN 20 " +
                    "     ELSE 0 " +
                    " END * :distanceWeight) + " +
                    " (COALESCE(ts.main_weight, 0) * :mainWeight) + " +
                    " ((COALESCE(rs.avg_scope, 0) * 20) * :reviewWeight) " +
                    "DESC " +
                    "LIMIT :limit", nativeQuery = true)
    List<TouristSpotWithDistance> findNearbyPopularSpots(
            @Param("userLat") double userLat, @Param("userLon") double userLon,
            @Param("distanceWeight") double distanceWeight, @Param("mainWeight") double mainWeight,
            @Param("reviewWeight") double reviewWeight, @Param("limit") int limit);

    /**
     * 1-a. 여행 계획이 있고, 위치 정보도 있는 경우
     */
    @Query(value =
            "SELECT ts.*, " +
                    " (6371 * acos(cos(radians(:userLat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(latitude)))) AS distance " +
                    "FROM tourist_spot ts " +
                    "LEFT JOIN (SELECT tourist_spot_id, AVG(scope) as avg_scope FROM tourist_spot_review GROUP BY tourist_spot_id) rs ON ts.tourist_spot_id = rs.tourist_spot_id " +
                    "WHERE ts.city_id = :cityId AND " +
                    " (6371 * acos(cos(radians(:userLat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(latitude)))) <= 70 " +
                    "ORDER BY " +
                    " (CASE " +
                    "     WHEN (6371 * acos(cos(radians(:userLat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(latitude)))) <= 5 THEN 100 " +
                    "     WHEN (6371 * acos(cos(radians(:userLat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(latitude)))) <= 15 THEN 80 " +
                    "     WHEN (6371 * acos(cos(radians(:userLat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(latitude)))) <= 30 THEN 50 " +
                    "     WHEN (6371 * acos(cos(radians(:userLat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(latitude)))) <= 60 THEN 20 " +
                    "     ELSE 0 " +
                    " END * :distanceWeight) + " +
                    " (COALESCE(ts.main_weight, 0) * :mainWeight) + " +
                    " ((COALESCE(rs.avg_scope, 0) * 20) * :reviewWeight) " +
                    "DESC " +
                    "LIMIT :limit", nativeQuery = true)
    List<TouristSpotWithDistance> findSpotsInCityWithScores(
            @Param("cityId") Long cityId,
            @Param("userLat") double userLat, @Param("userLon") double userLon,
            @Param("distanceWeight") double distanceWeight, @Param("mainWeight") double mainWeight,
            @Param("reviewWeight") double reviewWeight, @Param("limit") int limit
    );


    /**
     * 2-b. 예행 계획이 없고, 위치 정보도 없는 경우
     */
    @Query(value =
            "SELECT ts.* FROM tourist_spot ts " +
                    "LEFT JOIN (SELECT tourist_spot_id, AVG(scope) as avg_scope FROM tourist_spot_review GROUP BY tourist_spot_id) rs ON ts.tourist_spot_id = rs.tourist_spot_id " +
                    "ORDER BY " +
                    " (COALESCE(ts.main_weight, 0) * :mainWeight) + " +
                    " ((COALESCE(rs.avg_scope, 0) * 20) * :reviewWeight) " + // 리뷰 점수 (0~100 스케일)
                    "DESC " +
                    "LIMIT :limit", nativeQuery = true)
    List<TouristSpot> findSpotsByScoresWithoutLocation(
            @Param("mainWeight") double mainWeight,
            @Param("reviewWeight") double reviewWeight,
            @Param("limit") int limit);

    /**
     * 1-b. 여행 계획은 있지만, 위치 정보는 없는 경우
     */
    @Query(value =
            "SELECT ts.* FROM tourist_spot ts " +
                    "LEFT JOIN (SELECT tourist_spot_id, AVG(scope) as avg_scope FROM tourist_spot_review GROUP BY tourist_spot_id) rs ON ts.tourist_spot_id = rs.tourist_spot_id " +
                    "WHERE ts.city_id = :cityId " + // 도시 필터링 조건 추가
                    "ORDER BY " +
                    " (COALESCE(ts.main_weight, 0) * :mainWeight) + " +
                    " ((COALESCE(rs.avg_scope, 0) * 20) * :reviewWeight) " +
                    "DESC " +
                    "LIMIT :limit", nativeQuery = true)
    List<TouristSpot> findSpotsInCityByScoresWithoutLocation(
            @Param("cityId") Long cityId,
            @Param("mainWeight") double mainWeight,
            @Param("reviewWeight") double reviewWeight,
            @Param("limit") int limit);

}
