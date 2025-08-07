package com.nighttrip.core.domain.userspot.entity;

import com.nighttrip.core.domain.user.entity.User;
import com.nighttrip.core.global.converter.SpotCategoryConverter;
import com.nighttrip.core.global.converter.SpotDetailsConverter;
import com.nighttrip.core.global.enums.SpotCategory;
import com.nighttrip.core.global.enums.SpotDetails;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.EnumSet;

@Entity
@Table(name = "user_spot", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "spot_name"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "spot_name", nullable = false)
    private String spotName;

    @Column(name = "spot_memo", nullable = false)
    private String spotMemo;

    private Double latitude;
    private Double longitude;
    private String address;

    @Convert(converter = SpotCategoryConverter.class)
    private SpotCategory category;

    @Convert(converter = SpotDetailsConverter.class)
    @Column(name = "tourist_spot_details", columnDefinition = "TEXT")
    private EnumSet<SpotDetails> touristSpotDetails = EnumSet.noneOf(SpotDetails.class);


    public UserSpot(User user, String spotName, String spotMemo, String address, Double latitude, Double longitude, SpotCategory category, EnumSet<SpotDetails> details) {
        this.user = user;
        this.spotName = spotName;
        this.spotMemo = spotMemo;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category = category;
        this.touristSpotDetails = details;
    }
}

