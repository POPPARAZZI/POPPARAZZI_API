package com.spoons.popparazzi.popup.dto.query;

public record PopupViewCountQuery(
        String popupCode,
        long viewCount
) {
}