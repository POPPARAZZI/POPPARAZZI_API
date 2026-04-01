package com.spoons.popparazzi.popup.repository;

import com.spoons.popparazzi.popup.dto.query.PopupSelectionItemQuery;

import java.util.List;

public interface PopupSelectionQueryRepository {
    // 1. 모임 생성시 필요한 팝업 리스트(신규순)
    List<PopupSelectionItemQuery> findNewestSelections(int limit);
}
