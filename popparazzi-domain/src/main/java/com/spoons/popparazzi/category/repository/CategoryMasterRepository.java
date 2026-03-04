package com.spoons.popparazzi.category.repository;

import com.spoons.popparazzi.category.entity.CategoryMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CategoryMasterRepository extends JpaRepository<CategoryMaster, String> {
    // 모임 생성 시 categoryCodes(1~3개)가 실제 존재하는지 검증할 때 사용
    List<CategoryMaster> findAllByCodeIn(Collection<String> codes);
}
