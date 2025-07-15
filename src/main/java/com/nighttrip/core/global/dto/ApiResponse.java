package com.nighttrip.core.global.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record ApiResponse<T>(String message , T data,
                             @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
                             LocalDateTime timestamp) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", data, LocalDateTime.now());
    }

}
