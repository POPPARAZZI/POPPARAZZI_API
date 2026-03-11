package com.spoons.popparazzi.moim.dto.query.main;

import java.time.LocalDateTime;

public record RecommendedMoimBaseQuery(
        String moimCode,
        String popupCode,
        String sigungu,
        String title,
        LocalDateTime date,
        Integer maxParticipants,
        LocalDateTime regDt
) {
}