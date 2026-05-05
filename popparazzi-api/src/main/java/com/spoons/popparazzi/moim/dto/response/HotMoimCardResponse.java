package com.spoons.popparazzi.moim.dto.response;

import com.spoons.popparazzi.moim.dto.result.HotMoimCardResult;

import java.time.LocalDateTime;

public record HotMoimCardResponse(
        String moimCode,
        String title,
        LocalDateTime date,
        int currentParticipants,
        int maxParticipants,
        String thumbnailUrl,
        long likeCount
) {
    public static HotMoimCardResponse from(HotMoimCardResult result) {
        return new HotMoimCardResponse(
                result.moimCode(),
                result.title(),
                result.date(),
                result.currentParticipants(),
                result.maxParticipants(),
                result.thumbnailUrl(),
                result.likeCount()
        );
    }
}
