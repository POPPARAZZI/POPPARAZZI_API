package com.spoons.popparazzi.popup.repository;

import java.util.List;
import java.util.Map;

public interface PopupViewHistoryQueryRepository {

    long countViews(String popupCode);

    Map<String, Long> countViewsByPopupCodes(List<String> popupCodes);
}