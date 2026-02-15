package com.spoons.popparazzi.file.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class FileTypeConverter implements AttributeConverter<FileType, String> {

    @Override
    public String convertToDatabaseColumn(FileType attribute) {
        if (attribute == null) return null;
        return attribute.name(); // P, M, R
    }

    @Override
    public FileType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return null;
        return FileType.valueOf(dbData.trim()); // char 패딩 방어
    }

}
