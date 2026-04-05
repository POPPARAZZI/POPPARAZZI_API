package com.spoons.popparazzi.popup;

import com.spoons.popparazzi.response.ApiResponse;
import com.spoons.popparazzi.popup.dto.response.PopupSelectionItemResponse;
import com.spoons.popparazzi.popup.service.PopupSelectionService;
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
}