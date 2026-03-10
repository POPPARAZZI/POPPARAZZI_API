package com.spoons.popparazzi.moim.dto.query;

import java.time.LocalDateTime;

public record MoimDetailQuery(
        String moimCode,
        String title,
        String content,
        LocalDateTime moimDate,
        int maxParticipants,
        String leaderMemberCode,
        String leaderProfileUrl
) {
}