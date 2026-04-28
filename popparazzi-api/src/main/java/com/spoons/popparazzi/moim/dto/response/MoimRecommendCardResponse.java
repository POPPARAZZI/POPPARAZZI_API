package com.spoons.popparazzi.moim.dto.response;

import com.spoons.popparazzi.moim.dto.result.MoimRecommendCardResult;

import java.time.LocalDateTime;

public record MoimRecommendCardResponse(
        String moimCode,
        String title,
        LocalDateTime date,
        int currentParticipants,
        int maxParticipants,
        String thumbnailUrl,
        boolean liked
) {
    public static MoimRecommendCardResponse from(MoimRecommendCardResult result) {
        return new MoimRecommendCardResponse(
                result.moimCode(),
                result.title(),
                result.date(),
                result.currentParticipants(),
                result.maxParticipants(),
                result.thumbnailUrl(),
                result.liked()
        );
    }
}
