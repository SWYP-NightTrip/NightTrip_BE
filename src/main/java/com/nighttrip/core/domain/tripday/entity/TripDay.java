package com.nighttrip.core.domain.tripday.entity;

import com.nighttrip.core.domain.city.entity.City;
import com.nighttrip.core.domain.triporder.entity.TripOrder;
import com.nighttrip.core.domain.tripplan.entity.TripPlan;
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

    @OneToMany(mappedBy = "tripDay")
    private List<City> cities = new ArrayList<>();

    @OneToMany(mappedBy = "tripDay", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<TripOrder> tripOrders = new ArrayList<>();

    public void changeTripOrder(int fromIndex, int toIndex) {
        if (fromIndex == toIndex || fromIndex < 0 || toIndex < 0 ||
            fromIndex >= tripOrders.size() || toIndex >= tripOrders.size()) {
            return;
        }

        TripOrder moving = tripOrders.remove(fromIndex);
        tripOrders.add(toIndex, moving);

        BigDecimal prev = (toIndex == 0)
                ? BigDecimal.ZERO
                : tripOrders.get(toIndex - 1).getOrderIndex();

        BigDecimal next = (toIndex == tripOrders.size() - 1)
                ? prev.add(BigDecimal.valueOf(10000))
                : tripOrders.get(toIndex + 1).getOrderIndex();

        BigDecimal newOrder = prev.add(next)
                .divide(BigDecimal.valueOf(2), 6, RoundingMode.HALF_UP);

        moving.changeOrderIndex(newOrder);
    }
}
