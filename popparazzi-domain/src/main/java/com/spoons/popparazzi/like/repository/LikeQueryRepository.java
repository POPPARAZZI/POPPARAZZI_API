package com.spoons.popparazzi.like.repository;

import com.spoons.popparazzi.like.dto.query.LikeRankQuery;
import com.spoons.popparazzi.like.enums.LikeType;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface LikeQueryRepository {

    // 1) 핫 랭킹 조회 (since 이후)
    List<LikeRankQuery> findTopRankKeys(
            LikeType type,
            LocalDateTime since,
            Pageable pageable
    );

    // 2) 좋아요 여부 조회 (범용)
    List<String> findLikedTargetCodes(
            String memberCode,
            LikeType type,
            List<String> targetCodes
    );

    // 3) 좋아요수 전체 벌크 조회
    Map<String, Long> countTargetsByType(LikeType type, List<String> targetCodes);
}