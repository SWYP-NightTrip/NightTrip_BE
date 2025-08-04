package com.nighttrip.core.global.converter;

import com.nighttrip.core.global.enums.SpotCategory;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SpotCategoryConverter implements AttributeConverter<SpotCategory, String> {

    @Override
    public String convertToDatabaseColumn(SpotCategory attribute) {
        if (attribute == null) return null;
        return attribute.getKoreanName(); // DB에는 한글로 저장
    }

    @Override
    public SpotCategory convertToEntityAttribute(String dbData) {
        return SpotCategory.fromValue(dbData); // DB -> enum 변환
    }
}
