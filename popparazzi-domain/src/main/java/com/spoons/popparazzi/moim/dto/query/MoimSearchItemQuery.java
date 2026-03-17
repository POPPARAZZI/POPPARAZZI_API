package com.spoons.popparazzi.moim.dto.query;

public record MoimSearchItemQuery(
        String moimCode,
        String popupCode,
        String title,
        String address,
        String leaderMemberCode,
        String leaderNickname,
        Integer maxCount
) {
}