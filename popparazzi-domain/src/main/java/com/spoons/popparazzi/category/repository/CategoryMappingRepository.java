package com.spoons.popparazzi.category.repository;

import com.spoons.popparazzi.category.entity.CategoryMapping;
import com.spoons.popparazzi.category.entity.CategoryMappingId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryMappingRepository extends JpaRepository<CategoryMapping, CategoryMappingId> {
    void deleteByParentCode(String parentCode);
}
