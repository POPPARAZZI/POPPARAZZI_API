package com.spoons.popparazzi.moim.repository.hot;

import com.spoons.popparazzi.moim.dto.result.HotMoimCardResult;

import java.util.List;

public interface HotMoimQueryRepository {

    List<HotMoimCardResult> findHotCardsBase(List<String> mmCodes);
}