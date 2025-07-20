package com.nighttrip.core.domain.triporder.entity;


import com.nighttrip.core.domain.memo.entity.Memo;
import com.nighttrip.core.domain.tripday.entity.TripDay;
import com.nighttrip.core.domain.touristspot.entity.TouristSpot;
import com.nighttrip.core.global.enums.ItemType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trip_order")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TripOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_order_id")
    private Long id;

    @Column(name = "trip_order_index", precision = 12, scale = 6)
    private BigDecimal orderIndex;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemType itemType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_day_id")
    private TripDay tripDay;

    // 이거는 cascade 할지 말지 고민됩니다.
    @OneToMany(mappedBy = "tripOrder")
    private List<Memo> memos = new ArrayList<>();

    @OneToMany(mappedBy = "tripOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TouristSpot> touristSpots = new ArrayList<>();


    @Builder
    public TripOrder(BigDecimal orderIndex, ItemType itemType, TripDay tripDay) {
        this.orderIndex = orderIndex;
        this.itemType = itemType;
        this.tripDay = tripDay;
    }

    public void changeOrderIndex(BigDecimal orderIndex) {
        this.orderIndex = orderIndex;
    }
}
