package com.spoons.popparazzi.moim.repository.hot;

import com.spoons.popparazzi.like.enums.LikeType;
import com.spoons.popparazzi.moim.dto.result.HotMoimCardResult;
import com.spoons.popparazzi.moim.dto.query.hot.HotMoimRankQuery;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface HotMoimQueryRepository {

    /**
     * 24시간(또는 since 이후) 좋아요 기준 핫 모임 랭킹 Top N
     * - 반환: (moimCode, likeCount24h)
     */
    List<HotMoimRankQuery> findHotRankKeys(LikeType type, LocalDateTime since, Pageable pageable);

    /* 핫한 모임 카드 */
    List<HotMoimCardResult> findHotCardsBase(List<String> mmCodes);
}
