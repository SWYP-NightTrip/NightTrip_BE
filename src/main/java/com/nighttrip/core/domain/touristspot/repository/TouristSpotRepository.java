package com.nighttrip.core.domain.touristspot.repository;

import com.nighttrip.core.domain.city.entity.City;
import com.nighttrip.core.domain.touristspot.dto.TouristSpotWithDistance;
import com.nighttrip.core.domain.touristspot.entity.TouristSpot;
import com.nighttrip.core.global.enums.ImageSizeType;
import com.nighttrip.core.global.enums.ImageType;
import com.nighttrip.core.global.enums.SpotCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TouristSpotRepository extends JpaRepository<TouristSpot, Long>, TouristSpotQueryRepository {

    @Query("""
                select max(t.orderIndex)
                from TripOrder t
                where t.tripDay.tripPlan.id=:tripPlanId
                and t.tripDay.dayOrder=:tripDayOrder
            """)
    Optional<Long> findLastOrder(@Param("tripPlanId") Long tripPlanId, @Param("tripDayOrder") Integer tripDayOrder);

    @Query("SELECT ts FROM TouristSpot ts " +
            "LEFT JOIN ts.tourLikes tl " +
            "LEFT JOIN ts.touristSpotReviews tsr " +
            "LEFT JOIN ts.bookMarks bm " +
            "WHERE ts.city.id = :cityId " +
            "GROUP BY ts.id, ts.spotName, ts.address, ts.category, ts.spotDescription, " +
            "ts.longitude, ts.latitude, ts.checkCount, ts.link, ts.telephone, " +
            "ts.mainWeight, ts.subWeight, ts.city " +
            "ORDER BY (COALESCE(COUNT(DISTINCT tl.id), 0) + " +
            "COALESCE(COUNT(DISTINCT tsr.reviewId), 0) + " +
            "COALESCE(COUNT(DISTINCT bm.id), 0)) DESC")
    List<TouristSpot> findPopularTouristSpotsByCityId(@Param("cityId") Long cityId, Pageable pageable);


    @Query("SELECT ts FROM TouristSpot ts " +
            "LEFT JOIN FETCH ImageUrl i " +
            "WHERE ts.city.id = i.relatedId and i.imageType = 'CITY' " +
            "ORDER BY (COALESCE(ts.checkCount, 0) + COALESCE(ts.mainWeight, 0) + COALESCE(ts.subWeight, 0)) DESC, " +
            "ts.category ASC")
    List<TouristSpot> findRecommendedTouristSpotsByCityId(@Param("cityId") Long cityId, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"city"})
        // ★★★ 이 부분을 추가 또는 확인! ★★★
    List<TouristSpot> findAll();

    @Query("SELECT DISTINCT t.category FROM TouristSpot t ORDER BY t.category ASC")
    List<SpotCategory> findAllDistinctCategories();

    /**
     * 2-a. 여행계획이 없고, 위치 정보가 있는 경우
     */
    @Query(value = """
        SELECT ts.*, (6371 * acos(cos(radians(:userLat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(latitude)))) AS distance
        FROM tourist_spot ts
        LEFT JOIN (SELECT tourist_spot_id, AVG(scope) as avg_scope FROM tourist_spot_review GROUP BY tourist_spot_id) rs ON ts.tourist_spot_id = rs.tourist_spot_id
        WHERE (6371 * acos(cos(radians(:userLat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(latitude)))) <= 70
        ORDER BY (CASE WHEN (6371 * acos(cos(radians(:userLat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(latitude)))) <= 5 THEN 100 WHEN (6371 * acos(cos(radians(:userLat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(latitude)))) <= 15 THEN 80 WHEN (6371 * acos(cos(radians(:userLat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(latitude)))) <= 30 THEN 50 WHEN (6371 * acos(cos(radians(:userLat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(latitude)))) <= 60 THEN 20 ELSE 0 END * :distanceWeight) + (COALESCE(ts.main_weight, 0) * :mainWeight) + ((COALESCE(rs.avg_scope, 0) * 20) * :reviewWeight) DESC, ts.tourist_spot_id ASC
        LIMIT :limit
        """, nativeQuery = true)
    List<TouristSpotWithDistance> findNearbyPopularSpots(
            @Param("userLat") double userLat, @Param("userLon") double userLon,
            @Param("distanceWeight") double distanceWeight, @Param("mainWeight") double mainWeight,
            @Param("reviewWeight") double reviewWeight, @Param("limit") int limit);

    /**
     * 1-a. 여행 계획이 있고, 위치 정보도 있는 경우
     */
    @Query(value = """
        SELECT ts.*, (6371 * acos(cos(radians(:userLat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(latitude)))) AS distance
        FROM tourist_spot ts
        LEFT JOIN (SELECT tourist_spot_id, AVG(scope) as avg_scope FROM tourist_spot_review GROUP BY tourist_spot_id) rs ON ts.tourist_spot_id = rs.tourist_spot_id
        WHERE ts.city_id = :cityId AND (6371 * acos(cos(radians(:userLat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(latitude)))) <= 70
        ORDER BY (CASE WHEN (6371 * acos(cos(radians(:userLat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(latitude)))) <= 5 THEN 100 WHEN (6371 * acos(cos(radians(:userLat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(latitude)))) <= 15 THEN 80 WHEN (6371 * acos(cos(radians(:userLat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(latitude)))) <= 30 THEN 50 WHEN (6371 * acos(cos(radians(:userLat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(latitude)))) <= 60 THEN 20 ELSE 0 END * :distanceWeight) + (COALESCE(ts.main_weight, 0) * :mainWeight) + ((COALESCE(rs.avg_scope, 0) * 20) * :reviewWeight) DESC, ts.tourist_spot_id ASC
        LIMIT :limit
        """, nativeQuery = true)
    List<TouristSpotWithDistance> findSpotsInCityWithScores(
            @Param("cityId") Long cityId,
            @Param("userLat") double userLat, @Param("userLon") double userLon,
            @Param("distanceWeight") double distanceWeight, @Param("mainWeight") double mainWeight,
            @Param("reviewWeight") double reviewWeight, @Param("limit") int limit
    );


    /**
     * 2-b. 예행 계획이 없고, 위치 정보도 없는 경우
     */
    @Query(value = """
        SELECT ts.* FROM tourist_spot ts
        LEFT JOIN (SELECT tourist_spot_id, AVG(scope) as avg_scope FROM tourist_spot_review GROUP BY tourist_spot_id) rs ON ts.tourist_spot_id = rs.tourist_spot_id
        ORDER BY (COALESCE(ts.main_weight, 0) * :mainWeight) + ((COALESCE(rs.avg_scope, 0) * 20) * :reviewWeight) DESC, ts.tourist_spot_id ASC
        LIMIT :limit
        """, nativeQuery = true)
    List<TouristSpot> findSpotsByScoresWithoutLocation(
            @Param("mainWeight") double mainWeight,
            @Param("reviewWeight") double reviewWeight,
            @Param("limit") int limit);

    /**
     * 1-b. 여행 계획은 있지만, 위치 정보는 없는 경우
     */
    @Query(value = """
        SELECT ts.* FROM tourist_spot ts
        LEFT JOIN (SELECT tourist_spot_id, AVG(scope) as avg_scope FROM tourist_spot_review GROUP BY tourist_spot_id) rs ON ts.tourist_spot_id = rs.tourist_spot_id
        WHERE ts.city_id = :cityId
        ORDER BY (COALESCE(ts.main_weight, 0) * :mainWeight) + ((COALESCE(rs.avg_scope, 0) * 20) * :reviewWeight) DESC, ts.tourist_spot_id ASC
        LIMIT :limit
        """, nativeQuery = true)
    List<TouristSpot> findSpotsInCityByScoresWithoutLocation(
            @Param("cityId") Long cityId,
            @Param("mainWeight") double mainWeight,
            @Param("reviewWeight") double reviewWeight,
            @Param("limit") int limit);


    /**
     * 카테고리 추천용: 위치 기반 + 사용자 선호 카테고리 기반 추천
     */
    @Query(value = """
        SELECT ts.*, (6371 * acos(cos(radians(:userLat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(latitude)))) AS distance
        FROM tourist_spot ts
        WHERE ts.category = :category AND (6371 * acos(cos(radians(:userLat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(latitude)))) <= 70
        ORDER BY (COALESCE(ts.sub_weight, 0) * :subWeightParam) + (CASE WHEN (6371 * acos(cos(radians(:userLat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(latitude)))) <= 5 THEN 100 WHEN (6371 * acos(cos(radians(:userLat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(latitude)))) <= 15 THEN 80 WHEN (6371 * acos(cos(radians(:userLat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(latitude)))) <= 30 THEN 50 WHEN (6371 * acos(cos(radians(:userLat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(latitude)))) <= 60 THEN 20 ELSE 0 END * :distanceWeight) DESC, ts.tourist_spot_id ASC
        LIMIT :limit
        """, nativeQuery = true)
    List<TouristSpotWithDistance> findSpotsByCategoryAndLocation(
            @Param("category") SpotCategory category,
            @Param("userLat") double userLat, @Param("userLon") double userLon,
            @Param("subWeightParam") double subWeightParam,
            @Param("distanceWeight") double distanceWeight,
            @Param("limit") int limit);

    /**
     * 카테고리 추천용 (여행 계획 중): 해당 도시 & 카테고리 내에서 sub_weight로 정렬
     */
    Page<TouristSpot> findByCityAndCategoryOrderBySubWeightDescIdAsc(City city, SpotCategory category, Pageable pageable);

    /**
     * 카테고리 추천용 (위치 정보 없을 때 폴백): 전국 단위로 해당 카테고리 내에서 sub_weight로 정렬
     */
    Page<TouristSpot> findByCategoryOrderBySubWeightDescIdAsc(SpotCategory category, Pageable pageable);


    // --- "더보기(페이지네이션)"을 위한 새로운 메소드들 ---


    // [페이지네이션] 1-b. 여행 계획 O, 위치 X
    @Query(value = """
        SELECT ts FROM TouristSpot ts
        LEFT JOIN ts.touristSpotReviews tsr
        WHERE ts.city.id = :cityId
        GROUP BY ts.id
        ORDER BY (CAST(COALESCE(ts.mainWeight, 0) AS double) * :mainWeight) + ((COALESCE(AVG(tsr.scope), 0.0) * 20) * :reviewWeight) DESC, ts.id ASC
        """,
            countQuery = "SELECT COUNT(ts) FROM TouristSpot ts WHERE ts.city.id = :cityId")
    Page<TouristSpot> findSpotsInCityByScoresWithoutLocationPaginated(@Param("cityId") Long cityId, @Param("mainWeight") double mainWeight, @Param("reviewWeight") double reviewWeight, Pageable pageable);

    // [페이지네이션] 2-b. 여행 계획 X, 위치 X
    @Query(value = """
        SELECT ts FROM TouristSpot ts
        LEFT JOIN ts.touristSpotReviews tsr
        GROUP BY ts.id
        ORDER BY (CAST(COALESCE(ts.mainWeight, 0) AS double) * :mainWeight) + ((COALESCE(AVG(tsr.scope), 0.0) * 20) * :reviewWeight) DESC, ts.id ASC
        """,
            countQuery = "SELECT COUNT(ts) FROM TouristSpot ts")
    Page<TouristSpot> findSpotsByScoresWithoutLocationPaginated(@Param("mainWeight") double mainWeight, @Param("reviewWeight") double reviewWeight, Pageable pageable);

    // [페이지네이션] 2-a. 여행 계획 X, 위치 O
    @Query(value = """
        SELECT ts.*, (6371 * acos(cos(radians(:userLat)) * cos(radians(ts.latitude)) * cos(radians(ts.longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(ts.latitude)))) AS distance
        FROM tourist_spot ts
        LEFT JOIN (SELECT tourist_spot_id, AVG(scope) as avg_scope FROM tourist_spot_review GROUP BY tourist_spot_id) rs ON ts.tourist_spot_id = rs.tourist_spot_id
        WHERE (6371 * acos(cos(radians(:userLat)) * cos(radians(ts.latitude)) * cos(radians(ts.longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(ts.latitude)))) <= 70
        ORDER BY (CASE WHEN (6371 * acos(cos(radians(:userLat)) * cos(radians(ts.latitude)) * cos(radians(ts.longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(ts.latitude)))) <= 5 THEN 100 WHEN (6371 * acos(cos(radians(:userLat)) * cos(radians(ts.latitude)) * cos(radians(ts.longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(ts.latitude)))) <= 15 THEN 80 WHEN (6371 * acos(cos(radians(:userLat)) * cos(radians(ts.latitude)) * cos(radians(ts.longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(ts.latitude)))) <= 30 THEN 50 WHEN (6371 * acos(cos(radians(:userLat)) * cos(radians(ts.latitude)) * cos(radians(ts.longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(ts.latitude)))) <= 60 THEN 20 ELSE 0 END * :distanceWeight) + (COALESCE(ts.main_weight, 0) * :mainWeight) + ((COALESCE(rs.avg_scope, 0) * 20) * :reviewWeight) DESC, ts.tourist_spot_id ASC
        LIMIT :limit OFFSET :offset
        """,
            nativeQuery = true)
    List<TouristSpotWithDistance> findNearbyPopularSpotsPaginated(@Param("userLat") double userLat, @Param("userLon") double userLon, @Param("distanceWeight") double distanceWeight, @Param("mainWeight") double mainWeight, @Param("reviewWeight") double reviewWeight, @Param("limit") int limit, @Param("offset") long offset);

    @Query(value = """
        SELECT ts.*, (6371 * acos(cos(radians(:userLat)) * cos(radians(ts.latitude)) * cos(radians(ts.longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(ts.latitude)))) AS distance
        FROM tourist_spot ts
        LEFT JOIN (SELECT tourist_spot_id, AVG(scope) as avg_scope FROM tourist_spot_review GROUP BY tourist_spot_id) rs ON ts.tourist_spot_id = rs.tourist_spot_id
        WHERE ts.city_id = :cityId AND (6371 * acos(cos(radians(:userLat)) * cos(radians(ts.latitude)) * cos(radians(ts.longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(ts.latitude)))) <= 70
        ORDER BY (CASE WHEN (6371 * acos(cos(radians(:userLat)) * cos(radians(ts.latitude)) * cos(radians(ts.longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(ts.latitude)))) <= 5 THEN 100 WHEN (6371 * acos(cos(radians(:userLat)) * cos(radians(ts.latitude)) * cos(radians(ts.longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(ts.latitude)))) <= 15 THEN 80 WHEN (6371 * acos(cos(radians(:userLat)) * cos(radians(ts.latitude)) * cos(radians(ts.longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(ts.latitude)))) <= 30 THEN 50 WHEN (6371 * acos(cos(radians(:userLat)) * cos(radians(ts.latitude)) * cos(radians(ts.longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(ts.latitude)))) <= 60 THEN 20 ELSE 0 END * :distanceWeight) + (COALESCE(ts.main_weight, 0) * :mainWeight) + ((COALESCE(rs.avg_scope, 0) * 20) * :reviewWeight) DESC, ts.tourist_spot_id ASC
        LIMIT :limit OFFSET :offset
        """,
            nativeQuery = true)
    List<TouristSpotWithDistance> findSpotsInCityWithScoresPaginated(@Param("cityId") Long cityId, @Param("userLat") double userLat, @Param("userLon") double userLon, @Param("distanceWeight") double distanceWeight, @Param("mainWeight") double mainWeight, @Param("reviewWeight") double reviewWeight, @Param("limit") int limit, @Param("offset") long offset);

    // [페이지네이션] 카테고리 추천 (위치 기반)
    @Query(value = """
        SELECT ts.*, (6371 * acos(cos(radians(:userLat)) * cos(radians(ts.latitude)) * cos(radians(ts.longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(ts.latitude)))) AS distance
        FROM tourist_spot ts
        WHERE ts.category = :category AND (6371 * acos(cos(radians(:userLat)) * cos(radians(ts.latitude)) * cos(radians(ts.longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(ts.latitude)))) <= 70
        ORDER BY (COALESCE(ts.sub_weight, 0) * :subWeightParam) + (CASE WHEN (6371 * acos(cos(radians(:userLat)) * cos(radians(ts.latitude)) * cos(radians(ts.longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(ts.latitude)))) <= 5 THEN 100 WHEN (6371 * acos(cos(radians(:userLat)) * cos(radians(ts.latitude)) * cos(radians(ts.longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(ts.latitude)))) <= 15 THEN 80 WHEN (6371 * acos(cos(radians(:userLat)) * cos(radians(ts.latitude)) * cos(radians(ts.longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(ts.latitude)))) <= 30 THEN 50 WHEN (6371 * acos(cos(radians(:userLat)) * cos(radians(ts.latitude)) * cos(radians(ts.longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(ts.latitude)))) <= 60 THEN 20 ELSE 0 END * :distanceWeight) DESC, ts.tourist_spot_id ASC
        LIMIT :limit OFFSET :offset
        """,
            nativeQuery = true)
    List<TouristSpotWithDistance> findSpotsByCategoryAndLocationPaginated(@Param("category") String category, @Param("userLat") double userLat, @Param("userLon") double userLon, @Param("subWeightParam") double subWeightParam, @Param("distanceWeight") double distanceWeight, @Param("limit") int limit, @Param("offset") long offset);

    // --- "더보기(페이지네이션)" 네이티브 쿼리를 위한 COUNT 쿼리들 ---

    /**
     * [COUNT] 2-a. 여행 계획 X, 위치 O
     * findNearbyPopularSpotsPaginated 쿼리의 전체 개수를 세는 쿼리입니다.
     */
    @Query(value = """
            SELECT count(*)
            FROM tourist_spot ts
            WHERE (6371 * acos(cos(radians(:userLat)) * cos(radians(ts.latitude)) * cos(radians(ts.longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(ts.latitude)))) <= 70
            """, nativeQuery = true)
    long countNearbyPopularSpots(@Param("userLat") double userLat, @Param("userLon") double userLon);

    /**
     * [COUNT] 1-a. 여행 계획 O, 위치 O
     * findSpotsInCityWithScoresPaginated 쿼리의 전체 개수를 세는 쿼리입니다.
     */
    @Query(value = """
            SELECT count(*)
            FROM tourist_spot ts
            WHERE ts.city_id = :cityId AND (6371 * acos(cos(radians(:userLat)) * cos(radians(ts.latitude)) * cos(radians(ts.longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(ts.latitude)))) <= 70
            """, nativeQuery = true)
    long countSpotsInCityWithScores(@Param("cityId") Long cityId, @Param("userLat") double userLat, @Param("userLon") double userLon);

    /**
     * [COUNT] 카테고리 추천 (위치 기반)
     * findSpotsByCategoryAndLocationPaginated 쿼리의 전체 개수를 세는 쿼리입니다.
     */
    @Query(value = """
            SELECT count(*)
            FROM tourist_spot ts
            WHERE ts.category = :category AND (6371 * acos(cos(radians(:userLat)) * cos(radians(ts.latitude)) * cos(radians(ts.longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(ts.latitude)))) <= 70
            """, nativeQuery = true)
    long countSpotsByCategoryAndLocation(@Param("category") String category, @Param("userLat") double userLat, @Param("userLon") double userLon);


    @Query(value = """
      SELECT ts.*, 
             (6371 * 2 * asin( sqrt(
                power(sin(radians((ts.latitude - :lat)/2)),2) +
                cos(radians(:lat))*cos(radians(ts.latitude))*
                power(sin(radians((ts.longitude - :lng)/2)),2)
             ))) AS dist_km
      FROM tourist_spot ts
      WHERE ts.city_id = :cityId
      AND (6371 * 2 * asin( sqrt(
                power(sin(radians((ts.latitude - :lat)/2)),2) +
                cos(radians(:lat))*cos(radians(ts.latitude))*
                power(sin(radians((ts.longitude - :lng)/2)),2)
          ))) <= :radiusKm
      ORDER BY COALESCE(ts.main_weight,0) DESC,
               COALESCE(ts.check_count,0) DESC,
               COALESCE(ts.sub_weight,0) DESC,
               dist_km ASC
      LIMIT :limit OFFSET :offset
      """, nativeQuery = true)
    List<Object[]> findCandidatesNative(
            @Param("cityId") Long cityId,
            @Param("lat") double lat, @Param("lng") double lng,
            @Param("radiusKm") double radiusKm,
            @Param("limit") int limit, @Param("offset") int offset);

    @Query(
            value = "select t from TouristSpot t where t.city.id = :cityId",
            countQuery = "select count(t) from TouristSpot t where t.city.id = :cityId"
    )
    Page<TouristSpot> findByCityId(@Param("cityId") Long cityId, Pageable pageable);


    /**
     * 특정 도시 및 여러 카테고리에 해당하는 여행지 목록을 페이지네이션으로 조회합니다. (IN 쿼리)
     * (예: "관광" 카테고리(자연, 문화, 역사 등) 조회 시 사용)
     * 정렬: subWeight 내림차순, id 오름차순
     */
    Page<TouristSpot> findByCityAndCategoryInOrderBySubWeightDescIdAsc(City city, List<SpotCategory> categories, Pageable pageable);
    @Query("""
        select
           ts.id,
           ts.spotName,
           ts.address,
           ts.category,
           ts.spotDescription,
           (
             select iu.url
             from ImageUrl iu
             where iu.relatedId = ts.id
               and iu.imageType = :imageType
               and iu.imageSizeType = :sizeType
           )
        from TouristSpot ts
        where ts.id in :ids
    """)
    List<Object[]> findRowsWithThumbByIds(
            @Param("ids") Collection<Long> ids,
            @Param("imageType") ImageType imageType,
            @Param("sizeType") ImageSizeType sizeType
    );
}