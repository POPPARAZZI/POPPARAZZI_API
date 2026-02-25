package com.spoons.popparazzi.category.entity;

import com.spoons.popparazzi.category.enums.CategoryType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_category_master")
@Getter
@NoArgsConstructor
public class CategoryMaster {

    @Id
    @Column(name = "tcm_code", length = 22)
    private String code;

    @Column(name = "tcm_name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "tcm_type", length = 1, nullable = false)
    private CategoryType type;
}
