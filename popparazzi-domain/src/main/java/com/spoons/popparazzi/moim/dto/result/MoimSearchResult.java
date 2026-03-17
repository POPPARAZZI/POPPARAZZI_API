package com.spoons.popparazzi.moim.dto.result;

import com.spoons.popparazzi.popup.dto.result.PopupSearchMatchResult;

import java.util.List;

public record MoimSearchResult(
        String keyword,
        PopupSearchMatchResult matchedPopup,
        List<MoimSearchCardResult> moims,
        int currentPage,
        int recordCountPerPage,
        int totalRecord,
        int totalPage
) {
}