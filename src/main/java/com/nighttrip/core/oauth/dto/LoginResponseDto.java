package com.nighttrip.core.oauth.dto;

public record LoginResponseDto(
        Long userId,
        String email,
        String nickname
) {}