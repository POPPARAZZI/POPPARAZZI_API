package com.spoons.popparazzi.moim.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record MoimDetailResponse(
        String moimCode,
        String title,
        String content,
        LocalDateTime moimDate,
        int maxParticipants,

        String leaderMemberCode,
        String leaderProfileUrl,

        List<MoimDetailImageResponse> images,

        long likeCount,
        boolean liked,

        int participantCount,
        int extraParticipantCount,

        boolean owner
) {
}