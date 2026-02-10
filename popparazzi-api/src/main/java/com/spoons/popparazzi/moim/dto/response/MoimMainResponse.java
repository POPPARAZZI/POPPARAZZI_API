package com.spoons.popparazzi.moim.dto.response;

import java.time.LocalDateTime;

public record MoimMainResponse(
        String mmCode,
        String mmTitle,
        LocalDateTime mmDate,
        Integer mmMaxParticipants
) {
}
