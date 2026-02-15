package com.spoons.popparazzi.category.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_category_mapping")
@Getter
@NoArgsConstructor
@IdClass(CategoryMappingId.class)
public class CategoryMapping {

    @Id
    @Column(name = "tcm_tcm_code")
    private String categoryCode;

    @Id
    @Column(name = "tcm_parent_code")
    private String parentCode;
}
