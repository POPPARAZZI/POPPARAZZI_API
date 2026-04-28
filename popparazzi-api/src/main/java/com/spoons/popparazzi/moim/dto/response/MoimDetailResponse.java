package com.spoons.popparazzi.moim.dto.response;

import com.spoons.popparazzi.moim.dto.result.MoimDetailResult;

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
    public static MoimDetailResponse from(MoimDetailResult result) {
        List<MoimDetailImageResponse> images = result.images().stream()
                .map(MoimDetailImageResponse::from)
                .toList();

        return new MoimDetailResponse(
                result.moimCode(),
                result.title(),
                result.content(),
                result.moimDate(),
                result.maxParticipants(),
                result.leaderMemberCode(),
                result.leaderProfileUrl(),
                images,
                result.likeCount(),
                result.liked(),
                result.participantCount(),
                result.extraParticipantCount(),
                result.owner()
        );
    }
}
