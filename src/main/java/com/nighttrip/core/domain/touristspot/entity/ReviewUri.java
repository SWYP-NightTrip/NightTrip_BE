package com.nighttrip.core.domain.touristspot.entity;

import com.nighttrip.core.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
@Getter
public class ReviewUri {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_uri_id")
    private Long reviewUriId;
    @Column(name = "image_uri")
    private String Uri;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="review_id")
    private TouristSpotReview review;
}
