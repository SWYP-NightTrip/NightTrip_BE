package com.nighttrip.core.domain.favoritespot.entity;

import com.nighttrip.core.domain.tripspot.entity.TouristSpot;
import com.nighttrip.core.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "favorite_spot")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FavoriteSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "favorite_spot_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tourist_spot_id")
    private TouristSpot touristSpot;
}
