package com.spoons.popparazzi.like.dto.response;

import com.spoons.popparazzi.like.enums.LikeType;

public record ToggleLikeResponse(
        String targetCode,
        LikeType type,
        boolean liked,
        long likeCount
) {
}
