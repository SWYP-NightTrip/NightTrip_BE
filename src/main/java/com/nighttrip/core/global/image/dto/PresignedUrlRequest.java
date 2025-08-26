package com.nighttrip.core.global.image.dto;

public record PresignedUrlRequest(
        String userId,
        String placeName,
        int index,
        String extension
) {}