package com.spoons.popparazzi.moim.repository;

import com.spoons.popparazzi.file.entity.FileType;
import com.spoons.popparazzi.like.entity.LikeType;
import com.spoons.popparazzi.moim.dto.query.FileThumbQuery;
import com.spoons.popparazzi.moim.dto.query.HotMoimCardQuery;
import com.spoons.popparazzi.moim.dto.query.HotMoimRankQuery;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface HotMoimQueryRepository {

    /**
     * 24시간(또는 since 이후) 좋아요 기준 핫 모임 랭킹 Top N
     * - 반환: (mmCode, likeCount24h)
     */
    List<HotMoimRankQuery> findHotRankKeys(LikeType type, LocalDateTime since, Pageable pageable);

    /* 썸네일 가져오기
    * 모임 썸네일 O -> 모임 썸네일
    * 모임 썸네일 X -> 팝업 썸네일 */
    List<FileThumbQuery> findFirstThumbs(FileType type, List<String> parentCodes);

    /* 핫한 모임 카드 */
    List<HotMoimCardQuery> findHotCardsBase(List<String> mmCodes);
}
