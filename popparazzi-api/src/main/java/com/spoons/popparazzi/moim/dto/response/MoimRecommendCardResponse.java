package com.spoons.popparazzi.moim.dto.response;

import java.time.LocalDateTime;

public record MoimRecommendCardResponse(
        String moimCode,
        String title,
        LocalDateTime date,
        int currentParticipants,
        int maxParticipants,
        String thumbnailUrl,
        boolean liked
) {}
