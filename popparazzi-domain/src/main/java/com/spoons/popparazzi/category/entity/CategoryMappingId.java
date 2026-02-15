package com.spoons.popparazzi.category.entity;

import java.io.Serializable;
import java.util.Objects;

public class CategoryMappingId implements Serializable {

    private String categoryCode;
    private String parentCode;

    public CategoryMappingId() {}

    public CategoryMappingId(String categoryCode, String parentCode) {
        this.categoryCode = categoryCode;
        this.parentCode = parentCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CategoryMappingId that)) return false;
        return Objects.equals(categoryCode, that.categoryCode) &&
                Objects.equals(parentCode, that.parentCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryCode, parentCode);
    }
}
