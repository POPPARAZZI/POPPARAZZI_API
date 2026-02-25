package com.spoons.popparazzi.moim.dto.query.hot;

public record HotMoimRankQuery(
        String moimCode,
        long likeCount24h
) {
}
