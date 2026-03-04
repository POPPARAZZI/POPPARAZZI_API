package com.spoons.popparazzi.like.dto.result;

import com.spoons.popparazzi.like.enums.LikeType;

public record LikeToggleResult(
        String targetCode,
        LikeType type,
        boolean liked,
        long likeCount
) {
}
