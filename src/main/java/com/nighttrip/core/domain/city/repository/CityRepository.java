package com.nighttrip.core.domain.city.repository;

import com.nighttrip.core.domain.city.entity.City;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CityRepository extends JpaRepository<City, Long> {

    List<City> findAllByCountryName(String countryName);
    List<City> findAllByCountryNameNot(String countryName);
    @Query("SELECT c FROM City c WHERE " +
            "LOWER(c.cityName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.countryName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<City> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}