package com.spoons.popparazzi.moim.dto.result;

import java.time.LocalDateTime;
import java.util.List;

public record NewestMoimCardResult(
        String moimCode,
        String popupCode,
        String title,
        LocalDateTime moimDate,
        Integer maxParticipantCount,
        String thumbnailUrl,
        boolean liked,
        List<String> categories,
        List<String> participantProfileUrls
) {}