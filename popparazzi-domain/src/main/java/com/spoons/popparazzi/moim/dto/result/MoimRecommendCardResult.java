package com.spoons.popparazzi.moim.dto.result;

import java.time.LocalDateTime;

public record MoimRecommendCardResult(
        String moimCode,
        String title,
        LocalDateTime date,
        int currentParticipants,
        int maxParticipants,
        String thumbnailUrl,
        boolean liked
) {
}