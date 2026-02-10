package com.spoons.popparazzi.like.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LikeTypeConverter implements AttributeConverter<LikeType, String> {

    @Override
    public String convertToDatabaseColumn(LikeType attribute) {
        if (attribute == null) return null;
        return attribute.name(); // M, P, R
    }

    @Override
    public LikeType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return null;
        return LikeType.valueOf(dbData.trim()); // char 패딩 방어
    }
}
