package com.spoons.popparazzi.moim.dto.response;

import com.spoons.popparazzi.popup.dto.response.PopupSearchMatchResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "모임 검색 응답")
public record MoimSearchResponse(

        @Schema(description = "검색어", example = "검색어")
        String keyword,

        @Schema(description = "상단 매칭 팝업")
        PopupSearchMatchResponse matchedPopup,

        @Schema(description = "모임 검색 결과 목록")
        List<MoimSearchItemResponse> moims,

        @Schema(description = "현재 페이지 번호", example = "1")
        int currentPage,

        @Schema(description = "페이지당 조회 개수", example = "10")
        int recordCountPerPage,

        @Schema(description = "전체 검색 결과 수", example = "26")
        int totalRecord,

        @Schema(description = "전체 페이지 수", example = "3")
        int totalPage
) {
}