package com.spoons.popparazzi.popup;

import com.spoons.popparazzi.popup.dto.command.PopupSearchMatchCommand;
import com.spoons.popparazzi.popup.dto.response.PopupSearchMatchResponse;
import com.spoons.popparazzi.popup.dto.response.PopupSelectionItemResponse;
import com.spoons.popparazzi.popup.dto.result.PopupSearchMatchResult;
import com.spoons.popparazzi.popup.service.PopupSearchService;
import com.spoons.popparazzi.popup.service.PopupSelectionService;
import com.spoons.popparazzi.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/popups")
public class PopupController {

    private final PopupSelectionService popupSelectionService;
    private final PopupSearchService popupSearchService;

    /**
     * 1. 모임 생성용 팝업 기본 리스트 조회
     */
    @GetMapping("/selection")
    public ApiResponse<List<PopupSelectionItemResponse>> getNewestSelectionItems(
            @RequestParam(defaultValue = "20") int limit
    ) {
        int safeLimit = Math.min(Math.max(limit, 1), 50);

        List<PopupSelectionItemResponse> response = popupSelectionService.getNewestSelectionItems(safeLimit)
                .stream()
                .map(PopupSelectionItemResponse::from)
                .toList();

        return ApiResponse.success(response);
    }

    /**
     * 2. 팝업 검색 - best match 단건 조회
     */
    @GetMapping("/search")
    public ApiResponse<PopupSearchMatchResponse> searchBestMatch(
            @RequestParam String keyword
    ) {
        PopupSearchMatchResult result = popupSearchService.findBestMatch(
                new PopupSearchMatchCommand(keyword)
        );

        PopupSearchMatchResponse response =
                (result != null) ? PopupSearchMatchResponse.from(result) : null;

        return ApiResponse.success(response);
    }
}