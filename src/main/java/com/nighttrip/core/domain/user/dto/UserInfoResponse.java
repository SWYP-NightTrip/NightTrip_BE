package com.nighttrip.core.domain.user.dto;

import com.nighttrip.core.domain.user.entity.User;
import lombok.Getter;

@Getter
public class UserInfoResponse {

    private final Long userId;
    private final String email;
    private final String nickname;
    private final String avatarImageUrl;
    private final String role;
    private final Integer point;

    public UserInfoResponse(User user) {
        this.userId = user.getId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.avatarImageUrl = user.getAvatar() != null ? user.getAvatar().getImageUrl() : null;
        this.role = user.getRole().name();
        this.point = user.getPoint();
    }
}
