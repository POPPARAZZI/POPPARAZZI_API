package com.spoons.popparazzi.board.dto.result;

public record HotBoardCardResult(
        String boardCode,
        String title,
        String thumbnailUrl,
        long likeCount
) {}