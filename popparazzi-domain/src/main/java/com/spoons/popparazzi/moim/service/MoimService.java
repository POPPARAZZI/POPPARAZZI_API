package com.spoons.popparazzi.moim.service;

import com.spoons.popparazzi.moim.dto.command.MoimFilterCommand;
import com.spoons.popparazzi.moim.dto.result.*;

import java.util.List;

public interface MoimService {

    // 신규 모임 조회
    List<NewestMoimCardResult> getNewestMoimsForMain(int limit, String memberCode);

    // 핫한 모임 조회
    List<HotMoimCardResult> getHotMoimCardsForMain(int limit);

    // 즐겨찾기 기반 모임
    List<MoimRecommendCardResult> recommendForMember(String memberCode);

    // 모임 상세 조회
    MoimDetailResult getMoimDetail(String moimCode, String memberCode);

    // 모임 필터링 조회
    MoimFilterSliceResult getMoimsByFilter(MoimFilterCommand command);

}

