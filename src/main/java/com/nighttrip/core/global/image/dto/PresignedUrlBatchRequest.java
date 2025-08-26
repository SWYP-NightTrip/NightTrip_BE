package com.nighttrip.core.global.image.dto;

public record PresignedUrlBatchRequest(
        String userId,
        String placeName,
        int count,
        String extension
) {}
