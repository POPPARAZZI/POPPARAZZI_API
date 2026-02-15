package com.spoons.popparazzi.file.repository;

import com.spoons.popparazzi.file.dto.query.FileThumbQuery;
import com.spoons.popparazzi.file.entity.FileType;

import java.util.List;

public interface FileThumbQueryRepository {

    /* 썸네일 가져오기
     * 모임 썸네일 O -> 모임 썸네일
     * 모임 썸네일 X -> 팝업 썸네일 */
    List<FileThumbQuery> findFirstThumbs(FileType type, List<String> parentCodes);
}
