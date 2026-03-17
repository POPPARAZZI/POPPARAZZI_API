package com.spoons.popparazzi.popup.service;

import com.spoons.popparazzi.popup.dto.command.PopupSearchMatchCommand;
import com.spoons.popparazzi.popup.dto.result.PopupSearchMatchResult;

public interface PopupSearchService {

    PopupSearchMatchResult findBestMatch(PopupSearchMatchCommand command);
}