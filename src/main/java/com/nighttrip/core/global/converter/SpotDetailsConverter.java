package com.nighttrip.core.global.converter;

import com.nighttrip.core.global.enums.SpotDetails;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

@Converter
public class SpotDetailsConverter implements AttributeConverter<EnumSet<SpotDetails>, String> {

    private static final String DELIMITER = ",";

    @Override
    public String convertToDatabaseColumn(EnumSet<SpotDetails> attribute) {
        if (attribute == null || attribute.isEmpty()) return "";
        return attribute.stream()
                .map(SpotDetails::getKoreanName) // 한글로 저장
                .collect(Collectors.joining(DELIMITER));
    }

    @Override
    public EnumSet<SpotDetails> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return EnumSet.noneOf(SpotDetails.class);

        return Arrays.stream(dbData.split(DELIMITER))
                .map(String::trim)
                .map(SpotDetails::fromValue) // 한글 -> enum
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(SpotDetails.class)));
    }
}
