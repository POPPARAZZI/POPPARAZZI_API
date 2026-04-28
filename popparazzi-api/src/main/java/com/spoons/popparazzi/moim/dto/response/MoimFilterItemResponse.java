package com.spoons.popparazzi.moim.dto.response;

import com.spoons.popparazzi.moim.dto.result.MoimFilterResult;

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
    public static MoimFilterItemResponse from(MoimFilterResult result) {
        return new MoimFilterItemResponse(
                result.moimCode(),
                result.thumbnailUrl(),
                result.title(),
                result.categories(),
                result.address(),
                result.moimDate(),
                result.leaderNickname(),
                result.participantCount(),
                result.maxParticipantCount(),
                result.isFull(),
                result.liked(),
                result.participantProfileUrls()
        );
    }
}
