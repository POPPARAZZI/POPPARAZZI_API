package com.spoons.popparazzi.moim.dto.query;

import java.time.LocalDateTime;

public record NewestMoimItemQuery(
        String mmCode,
        String mmTitle,
        LocalDateTime mmDate,
        Integer mmMaxParticipants
) {}
