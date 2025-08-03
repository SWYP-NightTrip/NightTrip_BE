package com.nighttrip.core.domain.userspot.entity;

import com.nighttrip.core.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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

    @ElementCollection
    @CollectionTable(
            name = "user_spot_category",
            joinColumns = @JoinColumn(name = "user_spot_id")
    )
    @Column(name = "category", nullable = false)
    private List<String> categories = new ArrayList<>();

    public UserSpot(User user, String spotName, String spotMemo, Double latitude, Double longitude, List<String> categories) {
        this.user = user;
        this.spotName = spotName;
        this.spotMemo = spotMemo;
        this.latitude = latitude;
        this.longitude = longitude;
        this.categories = categories;
    }
}

