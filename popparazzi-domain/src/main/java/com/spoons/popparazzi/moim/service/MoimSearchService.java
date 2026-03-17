package com.spoons.popparazzi.moim.service;

import com.spoons.popparazzi.moim.dto.command.MoimSearchCommand;
import com.spoons.popparazzi.moim.dto.result.MoimSearchResult;

public interface MoimSearchService {
    MoimSearchResult searchMoims(MoimSearchCommand command);
}