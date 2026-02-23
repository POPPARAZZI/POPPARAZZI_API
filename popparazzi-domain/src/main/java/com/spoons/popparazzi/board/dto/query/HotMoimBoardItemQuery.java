package com.spoons.popparazzi.board.dto.query;

public record HotMoimBoardItemQuery(
        String boardCode,
        String title,
        long likeCount
) {}