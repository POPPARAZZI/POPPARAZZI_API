package com.spoons.popparazzi.like.dto.query;

public record LikeCountQuery(
        String targetCode,
        long likeCount
) {
}
