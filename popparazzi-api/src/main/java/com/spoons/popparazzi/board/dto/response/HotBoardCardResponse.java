package com.spoons.popparazzi.board.dto.response;

public record HotBoardCardResponse(
        String boardCode,
        String title,
        String thunmbnailUrl,
        long likeCount
) {
}
