package com.nighttrip.core.global.converter;

import com.nighttrip.core.global.enums.SpotCategory;
import com.nighttrip.core.global.enums.SpotDetails;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SpotDetailsConverter implements AttributeConverter<SpotDetails, String> {

    @Override
    public String convertToDatabaseColumn(SpotDetails attribute) {
        if (attribute == null) return null;
        return attribute.getKoreanName(); // DB에는 한글로 저장
    }

    @Override
    public SpotDetails convertToEntityAttribute(String dbData) {
        return SpotDetails.fromValue(dbData); // DB -> enum 변환
    }
}
