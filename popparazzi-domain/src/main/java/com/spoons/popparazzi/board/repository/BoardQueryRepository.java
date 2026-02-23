package com.spoons.popparazzi.board.repository;

import com.spoons.popparazzi.board.dto.query.HotMoimBoardItemQuery;

import java.time.LocalDateTime;
import java.util.List;

public interface BoardQueryRepository {
    List<HotMoimBoardItemQuery> findHotMoimBoards(
            LocalDateTime from,
            LocalDateTime to
    );
}
