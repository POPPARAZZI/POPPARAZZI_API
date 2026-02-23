package com.spoons.popparazzi.common;

import com.spoons.popparazzi.common.YesNo;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class YesNoConverter implements AttributeConverter<YesNo, String> {

    @Override
    public String convertToDatabaseColumn(YesNo attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public YesNo convertToEntityAttribute(String dbData) {
        return YesNo.from(dbData);
    }
}
