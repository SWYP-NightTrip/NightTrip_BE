package com.nighttrip.core.domain.tripspot.entity;

import com.nighttrip.core.domain.city.entity.City;
import com.nighttrip.core.domain.favoritespot.entity.FavoriteSpot;
import com.nighttrip.core.domain.triporder.entity.TripOrder;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tourist_spot")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TouristSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tourist_spot_id")
    private Long id;


    @Column(name = "spot_name", nullable = false, length = 100)
    private String spotName;

    private Double longitude;
    private Double latitude;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_order_id")
    private TripOrder tripOrder;


    // 왜 1:n 인지 궁금합니다.
    // 경복궁을 여러명이 할수있어서 그럼?
    @OneToMany(mappedBy = "touristSpot")
    private List<FavoriteSpot> favoriteSpots = new ArrayList<>();

}