package com.nighttrip.core.domain.user.dto;

import com.nighttrip.core.domain.user.entity.User;
import com.nighttrip.core.global.image.entity.ImageUrl;
import lombok.Getter;

@Getter
public class UserInfoResponse {

    private final Long userId;
    private final String email;
    private final String nickname;
    private final String avatarImageUrl;
    private final String role;
    private final Integer point;

    public UserInfoResponse(User user, String avatarImageUrl) {
        this.userId = user.getId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.avatarImageUrl = avatarImageUrl;
        this.role = user.getRole().name();
        this.point = user.getPoint();
    }
}
