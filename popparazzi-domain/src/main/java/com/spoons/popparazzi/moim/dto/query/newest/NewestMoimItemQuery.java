package com.spoons.popparazzi.moim.dto.query.newest;

import java.time.LocalDateTime;

public record NewestMoimItemQuery(
        String moimCode,
        String popupCode,
        String title,
        LocalDateTime date,
        Integer maxParticipants
) {}
