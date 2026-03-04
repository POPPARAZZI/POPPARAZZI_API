package com.spoons.popparazzi.category.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_category_mapping")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(CategoryMappingId.class)
public class CategoryMapping {

    @Id
    @Column(name = "tcm_tcm_code", length = 22, nullable = false)
    private String categoryCode;

    @Id
    @Column(name = "tcm_parent_code", length = 22, nullable = false)
    private String parentCode;

    private CategoryMapping(String categoryCode, String parentCode) {
        this.categoryCode = categoryCode;
        this.parentCode = parentCode;
    }

    public static CategoryMapping of(String categoryCode, String parentCode) {
        return new CategoryMapping(categoryCode, parentCode);
    }
}