package com.nighttrip.core.domain.tripspot.entity;

import com.nighttrip.core.domain.city.entity.City;
import com.nighttrip.core.global.enums.SpotType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tourist_spots")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TouristSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tourist_spot_id")
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    @Column(name = "spot_name", nullable = false, length = 100)
    private String spotName;

    private Double longitude;
    private Double latitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "spot_type", length = 50)
    private SpotType spotType;

}