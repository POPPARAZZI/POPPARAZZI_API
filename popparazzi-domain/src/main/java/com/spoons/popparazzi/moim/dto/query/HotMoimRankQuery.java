package com.spoons.popparazzi.moim.dto.query;

public record HotMoimRankQuery(
        String moimCode,
        long likeCount24h
) {
}
