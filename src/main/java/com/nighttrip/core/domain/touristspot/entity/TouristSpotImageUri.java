package com.nighttrip.core.domain.touristspot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
@Getter
@Table(name = "tourist_spotImage_uri" )
public class TouristSpotImageUri {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_uri_id")
    private Long imageUriId;
    @Column(name = "image_type")
    private String imageType;
    @Column(name = "image_uri")
    private String uri;
    @Column(name = "is_main")
    private boolean isMain;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="tourist_spot_id")
    private TouristSpot touristSpot;
}
