package com.spoons.popparazzi.file.service;

import com.spoons.popparazzi.file.dto.query.MoimThumbTarget;

import java.util.List;
import java.util.Map;

public interface FileThumbnailService {

    /* 1. 모임 썸네일 조회
    * 모임 썸네일 X -> 팝업 썸네일 조회 */
    Map<String, String> getMoimThumbsWithPopupFallback(List<MoimThumbTarget> targets);
}