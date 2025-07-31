package com.nighttrip.core.global.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.nighttrip.core.global.exception.BusinessException;

public enum SpotCategory {
    FOOD("음식"),
    LEISURE_SPORTS("레저스포츠"),
    CULTURE("문화관광"),
    ACCOMMODATION("숙박"),
    NATURE("자연관광"),
    SHOPPING("쇼핑"),
    EXPERIENCE("체험관광"),
    HISTORY("역사관광"),
    ETC("기타관광");

    private final String koreanName;

    SpotCategory(String koreanName) {
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
    public static SpotCategory fromValue(String value) {
        for (SpotCategory category : values()) {
            if (category.koreanName.equals(value)) {
                return category;
            }
        }
        throw new BusinessException(ErrorCode.INVALID_PLACE_CATEGORY);
    }
}
