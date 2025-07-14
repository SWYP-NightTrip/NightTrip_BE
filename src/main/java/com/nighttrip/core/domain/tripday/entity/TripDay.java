package com.nighttrip.core.domain.tripday.entity;

import com.nighttrip.core.domain.city.entity.City;
import com.nighttrip.core.domain.triporder.entity.TripOrder;
import com.nighttrip.core.domain.tripplan.entity.TripPlan;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trip_day")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TripDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_day_id")
    private Long id;

    @Column(name = "trip_day_order")
    private Integer order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_plan_id")
    private TripPlan tripPlan;

    @OneToMany(mappedBy = "tripDay")
    private List<City> cities = new ArrayList<>();

    @OneToMany(mappedBy = "tripDay", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TripOrder> tripOrders = new ArrayList<>();
}
