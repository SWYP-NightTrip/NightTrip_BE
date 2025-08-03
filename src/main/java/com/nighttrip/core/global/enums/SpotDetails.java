package com.nighttrip.core.global.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum SpotDetails {
    PARKING_AVAILABLE("주차 가능"),
    PARKING_NOT_AVAILABLE("주차 불가능"),
    PET_AVAILABLE("반려동물 출입 가능"),
    PET_NOT_AVAILABLE("반려동물 출입 불가능"),
    WIFI_AVAILABLE("와이파이 사용 가능"),
    WIFI_NOT_AVAILABLE("와이파이 사용 불가능");

    private final String koreanName;

    SpotDetails(String koreanName) {
        this.koreanName = koreanName;
    }

    @JsonCreator
    public static SpotDetails fromValue(String value) {
        return Arrays.stream(values())
                .filter(v -> v.getKoreanName().equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown value: " + value));
    }

    public String getKoreanName() {
        return koreanName;
    }

    @JsonValue
    public String toValue() {
        return koreanName;
    }
}

