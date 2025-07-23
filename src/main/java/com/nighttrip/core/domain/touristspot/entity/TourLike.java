package com.nighttrip.core.domain.touristspot.entity;


import com.nighttrip.core.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "tour_like",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "tourist_spot_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TourLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tour_like_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tourist_spot_id", nullable = false)
    private TouristSpot touristSpot;

    @CreationTimestamp
    @Column(name = "liked_at", nullable = false, updatable = false)
    private LocalDateTime likedAt;

}