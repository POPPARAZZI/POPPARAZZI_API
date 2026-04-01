package com.spoons.popparazzi.popup.dto.result;

import java.time.LocalDateTime;

public record PopupSelectionItemResult(
        String popupCode,
        String title,
        String sido,
        String sigungu,
        LocalDateTime startDt,
        LocalDateTime endDt,
        long likeCount,
        long viewCount,
        String thumbnailUrl
) {
}
