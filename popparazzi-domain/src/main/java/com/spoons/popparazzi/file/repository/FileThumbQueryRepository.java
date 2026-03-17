package com.spoons.popparazzi.file.repository;

import com.spoons.popparazzi.file.dto.query.FileThumbQuery;
import com.spoons.popparazzi.file.enums.FileType;

import java.util.List;
import java.util.Optional;

public interface FileThumbQueryRepository {

    // 1. 범용 썸네일 기본 조회 (목록용)
    List<FileThumbQuery> findFirstThumbs(FileType type, List<String> parentCodes);

    // 2. 단건 대표 썸네일 조회
    Optional<FileThumbQuery> findFirstThumb(FileType type, String parentCode);
}