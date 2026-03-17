package com.spoons.popparazzi.popup.dto.result;

import java.time.LocalDateTime;

public record PopupSearchMatchResult(
        String popupCode,
        String thumbnailUrl,
        String title,
        String address,
        LocalDateTime startDt,
        LocalDateTime endDt,
        long likeCount,
        long viewCount
) {
}