package com.spoons.popparazzi.moim.dto.response;

import com.spoons.popparazzi.moim.dto.result.NewestMoimCardResult;

import java.time.LocalDateTime;
import java.util.List;

public record NewesCardResponse(
        String moimCode,
        String thumbnailUrl,
        boolean liked,
        List<String> categories,
        String title,
        LocalDateTime moimDate,
        List<String> participantProfileUrls,
        int maxParticipantCount
) {
    public static NewesCardResponse from(NewestMoimCardResult result) {
        return new NewesCardResponse(
                result.moimCode(),
                result.thumbnailUrl(),
                result.liked(),
                result.categories(),
                result.title(),
                result.moimDate(),
                result.participantProfileUrls(),
                result.maxParticipantCount()
        );
    }
}
