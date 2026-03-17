package com.spoons.popparazzi.popup.dto.query;

import java.time.LocalDateTime;

public record PopupSearchMatchQuery(
        String popupCode,
        String title,
        String address,
        LocalDateTime startDt,
        LocalDateTime endDt
) {
}