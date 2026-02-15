package com.spoons.popparazzi.moim.dto.query;

import java.time.LocalDateTime;
import java.util.List;

public record NewestMoimCardQuery(
        String moimCode,
        String popupCode,
        String title,
        LocalDateTime date,
        Integer maxParticipants,
        String thumbnailUrl,
        boolean liked,
        List<String> categories
) {}
