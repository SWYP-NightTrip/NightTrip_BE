package com.nighttrip.core.domain.city.entity;


import com.nighttrip.core.domain.tripday.entity.TripDay;
import com.nighttrip.core.domain.tripspot.entity.TouristSpot;
import jakarta.persistence.*;
import lombok.AccessLevel;
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

    @Column(name = "city_name", nullable = false, length = 50)
    private String cityName;

    @Column(name = "country_name", nullable = false, length = 50)
    private String countryName;

    private Double longitude;
    private Double latitude;

    @Column(name = "city_image_url")
    private String imageUrl;


    @OneToMany(mappedBy = "city", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TouristSpot> touristSpots = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_day_id")
    private TripDay tripDay;
}