package com.spoons.popparazzi.moim.dto.response;

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
) {}