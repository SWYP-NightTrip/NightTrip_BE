package com.nighttrip.core.domain.touristspot.entity;

import com.nighttrip.core.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Entity
@Getter
public class TouristSpotReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    private Integer scope;
    private String content;

    @Column(name = "sumnail_uri")
    private String thumbnailUri;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="tourist_spot_id")
    private TouristSpot touristSpot;

}
