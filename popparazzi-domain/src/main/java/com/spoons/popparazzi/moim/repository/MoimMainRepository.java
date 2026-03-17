package com.spoons.popparazzi.moim.repository;

import com.spoons.popparazzi.moim.dto.query.main.*;
import com.spoons.popparazzi.moim.dto.query.main.NewestMoimItemQuery;
import com.spoons.popparazzi.moim.dto.result.HotMoimCardResult;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MoimMainRepository {

    // 1. 즐겨찾기 기반 모임 추천
    /**
     * 최근 N일 동안 사용자가 좋아요한 모임(LikeType=M)을 기준으로
     * 팝업의 시군구를 집계하여 TopK를 반환한다.
     */
    List<PreferredSigunguQuery> findPreferredSigunguTop(
            String memberCode,
            int days,
            int topK
    );

    /**
     * 선호 지역 기반으로 1차 추천 후보 모임을 조회한다.
     * - 삭제되지 않은 모임
     * - 현재 시각 + 24시간 이후 일정
     * - 내가 좋아요한 모임 제외
     */
    List<RecommendedMoimBaseQuery> findRecommendMoimCandidates(
            List<String> sigunguPriority,
            String memberCode,
            int limit
    );

    /**
     * 최근 N일 동안 사용자가 좋아요한 모임들의 카테고리를 집계하여
     * 선호 카테고리 TopN을 반환한다.
     */
    List<PreferredCategoryQuery> findPreferredCategories(
            String memberCode,
            int days,
            int topN
    );

    /**
     * 후보 모임들의 카테고리를 (모임코드, 카테고리코드) 형태로 조회한다.
     * (CategoryType.M만 조회)
     */
    List<MoimCategoryLinkQuery> findMoimCategories(
            List<String> moimCodes
    );

    // 2. 지금 핫한 모임 추천
    List<HotMoimCardResult> findHotCardsBase(List<String> mmCodes);

    // 3. 신규 모임 추천
    List<NewestMoimItemQuery> findNewestForMain(Pageable pageable);
}
