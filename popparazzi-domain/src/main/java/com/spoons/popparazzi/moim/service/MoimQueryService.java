package com.spoons.popparazzi.moim.service;

import com.spoons.popparazzi.moim.dto.command.MoimFilterCommand;
import com.spoons.popparazzi.moim.dto.command.MoimSearchCommand;
import com.spoons.popparazzi.moim.dto.result.*;

import java.util.List;

public interface MoimQueryService {

    /**
     * @methodName  : getNewestMoimsForMain
     * @author      : seulgi Yang
     * @param       : limit, memberCode
     * @returnType  : List<NewestMoimCardResult>
     * @desc        : 메인 화면 신규 모임 카드 목록 조회
     */
    List<NewestMoimCardResult> getNewestMoimsForMain(int limit, String memberCode);

    /**
     * @methodName  : getHotMoimCardsForMain
     * @author      : seulgi Yang
     * @param       : limit
     * @returnType  : List<HotMoimCardResult>
     * @desc        : 메인 화면 핫 모임 카드 목록 조회 (폴백: 최신순)
     */
    List<HotMoimCardResult> getHotMoimCardsForMain(int limit);

    /**
     * @methodName  : recommendForMember
     * @author      : seulgi Yang
     * @param       : memberCode
     * @returnType  : List<MoimRecommendCardResult>
     * @desc        : 회원 선호 기반 모임 추천 (폴백: 최신순)
     */
    List<MoimRecommendCardResult> recommendForMember(String memberCode);

    /**
     * @methodName  : getMoimDetail
     * @author      : seulgi Yang
     * @param       : moimCode, memberCode
     * @returnType  : MoimDetailResult
     * @desc        : 모임 상세 조회
     */
    MoimDetailResult getMoimDetail(String moimCode, String memberCode);

    /**
     * @methodName  : getMoimsByFilter
     * @author      : seulgi Yang
     * @param       : MoimFilterCommand
     * @returnType  : MoimFilterSliceResult
     * @desc        : 모임 필터 조회 (NEW / HOT / FAVORITE, 폴백: NEW)
     */
    MoimFilterSliceResult getMoimsByFilter(MoimFilterCommand command);

    /**
     * @methodName  : searchMoims
     * @author      : seulgi Yang
     * @param       : MoimSearchCommand
     * @returnType  : MoimSearchResult
     * @desc        : 모임 키워드 검색
     */
    MoimSearchResult searchMoims(MoimSearchCommand command);
}
