package com.spoons.popparazzi.moim.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record MoimMainResponse(
        String moimCode,
        String title,
        LocalDateTime date,
        Integer maxParticipants,
        String thumbnailUrl,
        boolean liked,
        List<String> categories
) {}
