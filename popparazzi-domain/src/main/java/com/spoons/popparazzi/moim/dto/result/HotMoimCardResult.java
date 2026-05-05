package com.spoons.popparazzi.moim.dto.result;

import java.time.LocalDateTime;

public record HotMoimCardResult(
        String moimCode,
        String popupCode,
        String title,
        LocalDateTime date,
        int currentParticipants,
        int maxParticipants,
        String thumbnailUrl,
        long likeCount
) {}