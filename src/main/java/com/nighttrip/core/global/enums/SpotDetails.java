package com.nighttrip.core.global.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum SpotDetails {
    PARKING_AVAILABLE("parking", "주차 가능"),
    PARKING_NOT_AVAILABLE("parking", "주차 불가능"),
    PET_AVAILABLE("pet", "반려동물 출입 가능"),
    PET_NOT_AVAILABLE("pet", "반려동물 출입 불가능"),
    WIFI_AVAILABLE("wifi", "와이파이 사용 가능"),
    WIFI_NOT_AVAILABLE("wifi", "와이파이 사용 불가능");

    private final String typeKey;
    private final String koreanName;

    SpotDetails(String typeKey, String koreanName) {
        this.typeKey = typeKey;
        this.koreanName = koreanName;
    }

    public String getTypeKey() { return typeKey; }
    public String getKoreanName() { return koreanName; }

    @JsonCreator
    public static SpotDetails fromValue(String value) {
        return Arrays.stream(values())
                .filter(v -> v.getKoreanName().equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown value: " + value));
    }
}

