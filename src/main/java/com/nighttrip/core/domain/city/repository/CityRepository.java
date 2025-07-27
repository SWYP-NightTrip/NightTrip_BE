package com.nighttrip.core.domain.city.repository;

import com.nighttrip.core.domain.city.dto.CityPopularityDto;
import com.nighttrip.core.domain.city.entity.City;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CityRepository extends JpaRepository<City, Long> {

    @Query("SELECT c FROM City c WHERE " +
            "LOWER(c.cityName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<City> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT c FROM City c ORDER BY (c.cityPepoleVisitied + c.cityConsum) DESC")
    List<City> findCitiesOrderByRecommendedScore();


    @Query("SELECT new com.nighttrip.core.domain.city.dto.CityPopularityDto(" +
            "c.id, c.cityName, c.imageUrl, " +
            "(COALESCE(COUNT(DISTINCT tl.id), 0L) + " +
            "COALESCE(COUNT(DISTINCT tsr.reviewId), 0L) + " +
            "COALESCE(COUNT(DISTINCT bm.id), 0L) + " +
            "COALESCE(COUNT(DISTINCT tp.id), 0L))) " +
            "FROM City c " +
            "LEFT JOIN c.touristSpots ts " +
            "LEFT JOIN ts.tourLikes tl " +
            "LEFT JOIN ts.touristSpotReviews tsr " +
            "LEFT JOIN ts.bookMarks bm " +
            "LEFT JOIN c.tripDay td " +
            "LEFT JOIN td.tripPlan tp " +
            "GROUP BY c.id, c.cityName, c.imageUrl " +
            "ORDER BY (COALESCE(COUNT(DISTINCT tl.id), 0L) + " +
            "COALESCE(COUNT(DISTINCT tsr.reviewId), 0L) + " +
            "COALESCE(COUNT(DISTINCT bm.id), 0L) + " +
            "COALESCE(COUNT(DISTINCT tp.id), 0L)) DESC " +
            "LIMIT 7")
    List<CityPopularityDto> findPopularCitiesWithAggregatedScores();

}