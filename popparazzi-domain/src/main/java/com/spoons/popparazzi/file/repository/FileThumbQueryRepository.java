package com.spoons.popparazzi.file.repository;

import com.spoons.popparazzi.file.dto.query.FileThumbQuery;
import com.spoons.popparazzi.file.enums.FileType;

import java.util.List;

public interface FileThumbQueryRepository {

    // 1. 범용 썸네일 기본 조회
    List<FileThumbQuery> findFirstThumbs(FileType type, List<String> parentCodes);
}
