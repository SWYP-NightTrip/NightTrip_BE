package com.nighttrip.core.domain.tripspot.entity;

import com.nighttrip.core.domain.city.entity.City; // City 임포트
import com.nighttrip.core.domain.tripplan.entity.TripPlan;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "trip_cities")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TripCity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_cities_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_plan_id", nullable = false)
    private TripPlan tripPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @Column(name = "visit_order")
    private int visitOrder;
}