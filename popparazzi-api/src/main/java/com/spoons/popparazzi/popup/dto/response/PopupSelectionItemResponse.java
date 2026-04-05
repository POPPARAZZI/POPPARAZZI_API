package com.spoons.popparazzi.popup.dto.response;

import com.spoons.popparazzi.popup.dto.result.PopupSelectionItemResult;

import java.time.LocalDateTime;

public record PopupSelectionItemResponse(
        String popupCode,
        String title,
        String sido,
        String sigungu,
        LocalDateTime startDt,
        LocalDateTime endDt,
        Long likeCount,
        Long viewCount,
        String thumbnailUrl
) {
    public static PopupSelectionItemResponse from(PopupSelectionItemResult result) {
        return new PopupSelectionItemResponse(
                result.popupCode(),
                result.title(),
                result.sido(),
                result.sigungu(),
                result.startDt(),
                result.endDt(),
                result.likeCount(),
                result.viewCount(),
                result.thumbnailUrl()
        );
    }
}