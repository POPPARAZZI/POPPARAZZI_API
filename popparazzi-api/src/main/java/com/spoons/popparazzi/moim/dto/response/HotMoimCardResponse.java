package com.spoons.popparazzi.moim.dto.response;

import java.time.LocalDateTime;

public record HotMoimCardResponse(
        String mmCode,
        String title,
        LocalDateTime date,
        int currentParticipants,
        int maxParticipants,
        String thumbnailUrl,
        long likeCount24h
) {}
