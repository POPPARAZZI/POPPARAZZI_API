package com.spoons.popparazzi.moim.dto.query;

import java.time.LocalDateTime;

public record MoimFilterItemQuery(
        String moimCode,
        String popupCode,
        String title,
        String address,
        LocalDateTime moimDate,
        String leaderMemberCode,
        String leaderNickname,
        Integer maxParticipantCount,
        LocalDateTime regDt
) {
}