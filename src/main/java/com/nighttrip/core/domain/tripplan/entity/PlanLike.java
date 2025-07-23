package com.nighttrip.core.domain.tripplan.entity;

import com.nighttrip.core.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "plan_like",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "trip_plan_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlanLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_like_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_plan_id", nullable = false)
    private TripPlan tripPlan;

    @CreationTimestamp
    @Column(name = "liked_at", nullable = false, updatable = false)
    private LocalDateTime likedAt;

}