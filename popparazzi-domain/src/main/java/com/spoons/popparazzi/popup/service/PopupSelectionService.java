package com.spoons.popparazzi.popup.service;

import com.spoons.popparazzi.popup.dto.result.PopupSelectionItemResult;

import java.util.List;

public interface PopupSelectionService {
    // 1. 모임 생성시 필요한 팝업 리스트(신규순)
    List<PopupSelectionItemResult> getNewestSelectionItems(int limit);
}
