package com.spoons.popparazzi.board.service;

import com.spoons.popparazzi.board.dto.result.HotBoardCardResult;

import java.util.List;

public interface BoardMainService {
    List<HotBoardCardResult> getHotMoimBoardsForMain();
}
