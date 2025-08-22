package com.nighttrip.core.domain.tripday.entity;

import com.nighttrip.core.domain.city.entity.City;
import com.nighttrip.core.domain.tripplan.entity.TripPlan;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "city_on_trip_day")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CityOnTripDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "c_t_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_plan_id", nullable = false)
    private TripPlan tripPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    public CityOnTripDay(TripPlan tripPlan, City city) {
        this.tripPlan = tripPlan;
        this.city = city;
    }
}
