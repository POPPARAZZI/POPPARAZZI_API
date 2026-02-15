package com.spoons.popparazzi.category.repository;

import com.spoons.popparazzi.category.dto.query.MoimCategoryRow;

import java.util.List;

public interface CategoryQueryRepository {
    List<MoimCategoryRow> findMoimCategories(List<String> parentCodes);
}

