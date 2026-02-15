package com.spoons.popparazzi.like.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false) // ✅ LikeType에만 붙여서 쓰는 걸 추천 (안전)
public class LikeTypeConverter implements AttributeConverter<LikeType, String> {

    @Override
    public String convertToDatabaseColumn(LikeType attribute) {
        if (attribute == null) return null;
        return attribute.name(); // "M" / "P" / "R"
    }

    @Override
    public LikeType convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        String value = dbData.trim();
        if (value.isEmpty()) return null;
        return LikeType.valueOf(value);
    }
}
