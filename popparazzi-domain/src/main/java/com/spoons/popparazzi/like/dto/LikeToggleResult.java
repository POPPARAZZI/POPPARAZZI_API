package com.spoons.popparazzi.like.dto;

import com.spoons.popparazzi.like.entity.LikeType;

public record LikeToggleResult(
        String targetCode,
        LikeType type,
        boolean liked,
        long likeCount
) {
}
