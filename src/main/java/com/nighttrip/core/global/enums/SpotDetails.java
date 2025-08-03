package com.nighttrip.core.global.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.nighttrip.core.global.exception.BusinessException;

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

    public String getKoreanName() {
        return koreanName;
    }

    @JsonValue
    public String toValue() {
        return koreanName;
    }

    @JsonCreator
    public static SpotDetails fromValue(String value) {
        for (SpotDetails spotDetails : values()) {
            if (spotDetails.koreanName.equals(value)) {
                return spotDetails;
            }
        }
        throw new BusinessException(ErrorCode.INVALID_PLACE_CATEGORY);
    }

}
