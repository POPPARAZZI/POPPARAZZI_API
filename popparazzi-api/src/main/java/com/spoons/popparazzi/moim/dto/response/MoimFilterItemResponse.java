package com.spoons.popparazzi.moim.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record MoimFilterItemResponse(
        String moimCode,
        String thumbnailUrl,
        String title,
        List<String> categories,
        String address,
        LocalDateTime moimDate,
        String leaderNickname,
        int participantCount,
        int maxParticipantCount,
        boolean isFull,
        boolean liked,
        List<String> participantProfileUrls
) {
}