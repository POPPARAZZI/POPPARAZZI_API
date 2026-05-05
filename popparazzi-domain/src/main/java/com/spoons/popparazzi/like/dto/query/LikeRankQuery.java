package com.spoons.popparazzi.like.dto.query;

// targetCode = 모임코드/팝업코드/게시글코드 (LikeType으로 구분)
// likeCountToday = 오늘 00:00~현재 좋아요 수
public record LikeRankQuery(
        String targetCode,
        long likeCount
) {}