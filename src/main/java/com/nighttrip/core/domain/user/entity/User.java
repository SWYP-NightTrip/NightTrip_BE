package com.nighttrip.core.domain.user.entity;

import com.nighttrip.core.domain.avatar.entity.Avatar;
import com.nighttrip.core.domain.tripplan.entity.TripPlan;
import com.nighttrip.core.global.enums.Oauth_Provider;
import com.nighttrip.core.global.enums.User_role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = 100)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Oauth_Provider provider;

    @Column(name = "social_id", nullable = false)
    private String socialId;

    @Column(name = "point")
    private Integer point = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private User_role role = User_role.USER;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avatar_id")
    private Avatar avatar;

    @OneToMany(mappedBy = "user")
    private List<TripPlan> tripPlans = new ArrayList<>();

    public User(String email, String nickname, String socialId, Oauth_Provider provider) {
        this.email = email;
        this.nickname = nickname;
        this.socialId = socialId;
        this.provider = provider;
    }
}
