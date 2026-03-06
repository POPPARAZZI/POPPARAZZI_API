package com.spoons.popparazzi.file.repository;

import com.spoons.popparazzi.common.YesNo;
import com.spoons.popparazzi.file.entity.FileMaster;
import com.spoons.popparazzi.file.enums.FileType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileMasterRepository extends JpaRepository<FileMaster, Long> {

    // 1. 조건에 맞는 파일 조회
    List<FileMaster> findAllByParentCodeAndFmTypeAndDeleteYn(
            String parentCode,
            FileType fmType,
            YesNo deleteYn
    );
}