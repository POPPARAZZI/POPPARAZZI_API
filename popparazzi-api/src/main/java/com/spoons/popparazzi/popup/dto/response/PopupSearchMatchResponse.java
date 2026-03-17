package com.spoons.popparazzi.popup.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "모임 검색 상단 팝업 매칭 응답")
public record PopupSearchMatchResponse(

        @Schema(description = "팝업 코드", example = "P001")
        String popupCode,

        @Schema(description = "팝업 썸네일 URL", example = "https://cdn.popparazzi.com/popup/thumb.jpg")
        String thumbnailUrl,

        @Schema(description = "팝업명", example = "팝업명")
        String title,

        @Schema(description = "팝업 지역", example = "서울 성동구")
        String address,

        @Schema(description = "팝업 시작일시")
        LocalDateTime startDt,

        @Schema(description = "팝업 종료일시")
        LocalDateTime endDt,

        @Schema(description = "좋아요 수", example = "0")
        long likeCount,

        @Schema(description = "조회수", example = "0")
        long viewCount
) {
}