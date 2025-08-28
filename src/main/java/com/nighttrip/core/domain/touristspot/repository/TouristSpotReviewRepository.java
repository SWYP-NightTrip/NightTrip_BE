package com.nighttrip.core.domain.touristspot.repository;

import com.nighttrip.core.domain.touristspot.entity.TouristSpotReview;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TouristSpotReviewRepository extends JpaRepository<TouristSpotReview, Long> {

    @EntityGraph(attributePaths = {
            "touristSpot",
            "touristSpot.category",
            "user"
    })
    @Query("""
                select r
                from TouristSpotReview r
                where r.touristSpot.id = :touristSpotId
            """)
    List<TouristSpotReview> findByTouristSpotId(@Param("touristSpotId") Long touristSpotId);
}

