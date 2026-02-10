package com.spoons.popparazzi.moim.dto.query;

import java.time.LocalDateTime;

public record HotMoimCardQuery(
        String mmCode,
        String pmCode,
        String title,
        LocalDateTime date,
        int currentParticipants,
        int maxParticipants,
        String thumbnailUrl,
        long likeCount24h
) {}
