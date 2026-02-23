package com.spoons.popparazzi.file.repository;

import com.spoons.popparazzi.file.dto.query.FileThumbQuery;
import com.spoons.popparazzi.file.enums.FileType;

import java.util.List;

public interface FileThumbQueryRepository {

    // 1. 모임 전용
    List<FileThumbQuery> findFirstThumbs(FileType type, List<String> parentCodes);

    // 2. 게시판 전용
    List<FileThumbQuery> findFirstThumbsForPosts(List<String> parentCodes);
}
