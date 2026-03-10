package com.spoons.popparazzi.moim.dto.result;

import java.time.LocalDateTime;
import java.util.List;

public record MoimDetailResult(
        String moimCode,
        String title,
        String content,
        LocalDateTime moimDate,
        int maxParticipants,

        String leaderMemberCode,
        String leaderProfileUrl,

        List<MoimDetailImageResult> images,

        long likeCount,
        boolean liked,

        int participantCount,
        int extraParticipantCount,

        boolean owner
) {
}