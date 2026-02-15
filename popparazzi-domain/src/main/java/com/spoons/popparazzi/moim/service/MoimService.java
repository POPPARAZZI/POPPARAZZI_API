package com.spoons.popparazzi.moim.service;

import com.spoons.popparazzi.moim.dto.command.CreateMoimCommand;
import com.spoons.popparazzi.moim.dto.query.HotMoimCardQuery;
import com.spoons.popparazzi.moim.dto.query.HotMoimRankQuery;
import com.spoons.popparazzi.moim.dto.query.NewestMoimCardQuery;

import java.util.List;

public interface MoimService {
    Long create(CreateMoimCommand command);

    // 신규 모임 조회
    List<NewestMoimCardQuery> getNewestMoimsForMain(int limit, String memberCode);

    // 핫한 모임 10위 조회
    List<HotMoimRankQuery> getHotMoimsRanks(int limit);

    // 핫한 모임 조회
    List<HotMoimCardQuery> getHotMoimCardsForMain(int limit);

}
