package com.nighttrip.core.domain.tripplan.entity;

import com.nighttrip.core.domain.tripday.entity.CityOnTripDay;
import com.nighttrip.core.domain.tripday.entity.TripDay;
import com.nighttrip.core.domain.user.entity.User;
import com.nighttrip.core.global.enums.TripFeature;
import com.nighttrip.core.global.enums.TripStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trip_plan")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TripPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_plan_id")
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_shared")
    private boolean isShared = false;

    @Column(name = "num_index")
    private Long numIndex;

    @OneToMany(mappedBy = "tripPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CityOnTripDay> cityOnTripDays = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TripStatus status = TripStatus.UPCOMING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TripFeature isFeatured;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDate createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDate updatedAt;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "tripPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TripDay> tripDays = new ArrayList<>();

    public void changeStatus(TripStatus status) {
        this.status = status;
    }
    public void changeNumIndex(Long newIndex) {
        this.numIndex = newIndex;
    }
}