package com.nighttrip.core.domain.tripday.entity;

import com.nighttrip.core.domain.city.entity.City;
import com.nighttrip.core.domain.triporder.entity.TripOrder;
import com.nighttrip.core.domain.tripplan.entity.TripPlan;
import com.nighttrip.core.global.enums.ErrorCode;
import com.nighttrip.core.global.exception.BusinessException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private Integer dayOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_plan_id")
    private TripPlan tripPlan;

    @OneToMany(mappedBy = "tripDay", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CityOnTripDay> cityOnTripDays = new ArrayList<>();

    @OneToMany(mappedBy = "tripDay", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<TripOrder> tripOrders = new ArrayList<>();


    public void removeTripOrder(TripOrder tripOrder) {
        this.tripOrders.remove(tripOrder);
    }

    // ⭐ 추가된 메서드
    public void addTripOrderAt(TripOrder tripOrder, int toIndex) {
        if (toIndex < 0 || toIndex > tripOrders.size()) {
            throw new BusinessException(ErrorCode.INVALID_TRIP_ORDER_INDEX);
        }
        this.tripOrders.add(toIndex, tripOrder);
    }
}
