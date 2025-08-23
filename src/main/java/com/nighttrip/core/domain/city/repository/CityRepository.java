package com.nighttrip.core.domain.city.repository;

import com.nighttrip.core.domain.city.dto.CityPopularityDto;
import com.nighttrip.core.domain.city.entity.City;
import com.nighttrip.core.domain.tripplan.entity.TripPlan;
import com.nighttrip.core.domain.user.entity.User;
import com.nighttrip.core.global.enums.TripStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CityRepository extends JpaRepository<City, Long> {

    @Query("SELECT c FROM City c WHERE " +
            "LOWER(c.cityName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<City> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT c FROM City c ORDER BY (COALESCE(c.cityPepoleVisitied, 0.0) + COALESCE(c.cityConsum, 0.0)) DESC, c.id ASC")
    List<City> findCitiesOrderByRecommendedScore();

    @Query("SELECT c FROM City c ORDER BY (COALESCE(c.cityPepoleVisitied, 0.0) + COALESCE(c.cityConsum, 0.0)) DESC, c.id ASC")
    Page<City> findCitiesOrderByRecommendedScore(Pageable pageable);


    @Query("SELECT new com.nighttrip.core.domain.city.dto.CityPopularityDto(" +
            "c.id, c.cityName, " +
            "CAST(" +
            "(COALESCE(COUNT(DISTINCT tl.id), 0L) + " +
            "COALESCE(COUNT(DISTINCT tsr.reviewId), 0L) + " +
            "COALESCE(COUNT(DISTINCT bm.id), 0L) + " +
            "COALESCE(COUNT(DISTINCT tp.id), 0L)) AS long)" +
            ") " +
            "FROM City c " +
            "LEFT JOIN c.touristSpots ts " +
            "LEFT JOIN ts.tourLikes tl " +
            "LEFT JOIN ts.touristSpotReviews tsr " +
            "LEFT JOIN ts.bookMarks bm " +
            "LEFT JOIN c.cityOnTripDays cotd " +
            "LEFT JOIN cotd.tripPlan tp " +
            "GROUP BY c.id, c.cityName " +
            "ORDER BY (" +
            "COALESCE(COUNT(DISTINCT tl.id), 0L) + " +
            "COALESCE(COUNT(DISTINCT tsr.reviewId), 0L) + " +
            "COALESCE(COUNT(DISTINCT bm.id), 0L) + " +
            "COALESCE(COUNT(DISTINCT tp.id), 0L)) DESC")
    List<CityPopularityDto> findPopularCitiesWithAggregatedScores(Pageable pageable);
    List<City> findAllByOrderByIdAsc(Pageable pageable);

    List<City> findAllByCityNameIn(List<String> cityNames);

    @Query("SELECT c FROM City c WHERE c.cityName LIKE %:keyword%")
    List<City> findByCityNameWithLike(@Param("keyword") String keyword);
    Optional<City> findByCityName(String cityName);
}