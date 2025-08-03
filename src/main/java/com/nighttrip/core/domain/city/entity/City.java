// domain/city/entity/City.java (Lombok @Builder 사용)
package com.nighttrip.core.domain.city.entity;

import com.nighttrip.core.domain.touristspot.entity.TouristSpot;
import com.nighttrip.core.domain.tripday.entity.CityOnTripDay;
import com.nighttrip.core.domain.tripday.entity.TripDay;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "city")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "city_id")
    private Long id;

    @Column(name = "city_name", nullable = false, length = 100)
    private String cityName;
    private Integer checkCount;
    @Column(name = "city_consum")
    private Double cityConsum;
    @Column(name = "city_pepole_visitied")
    private Double cityPepoleVisitied;

    @Column(name = "city_image_url")
    private String imageUrl;

    @OneToMany(mappedBy = "city", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TouristSpot> touristSpots = new ArrayList<>();

    @OneToMany(mappedBy = "city")
    private List<CityOnTripDay> cityOnTripDays = new ArrayList<>();

    @Builder
    public City(String cityName, Integer checkCount, double cityConsum, double cityPepoleVisitied, String imageUrl) {
        this.cityName = cityName;
        this.checkCount = checkCount;
        this.cityConsum = cityConsum;
        this.cityPepoleVisitied = cityPepoleVisitied;
        this.imageUrl = imageUrl;
    }
}