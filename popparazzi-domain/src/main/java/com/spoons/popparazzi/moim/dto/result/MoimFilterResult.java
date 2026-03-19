package com.spoons.popparazzi.moim.dto.result;

import java.time.LocalDateTime;
import java.util.List;

public record MoimFilterResult(
        String moimCode,
        String thumbnailUrl,
        String title,
        List<String> categoryNames,
        String address,
        LocalDateTime moimDate,
        String leaderNickname,
        int participantCount,
        int maxParticipantCount,
        boolean isFull,
        boolean isLiked
) {
}