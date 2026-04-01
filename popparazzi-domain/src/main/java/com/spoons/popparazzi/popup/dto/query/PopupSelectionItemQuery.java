package com.spoons.popparazzi.popup.dto.query;

import java.time.LocalDateTime;

public record PopupSelectionItemQuery(
        String popupCode,
        String title,
        String sido,
        String sigungu,
        LocalDateTime startDt,
        LocalDateTime endDt
) {
}
