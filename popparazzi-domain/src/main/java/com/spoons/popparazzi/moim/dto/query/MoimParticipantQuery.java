package com.spoons.popparazzi.moim.dto.query;

public record MoimParticipantQuery(
        String memberCode,
        String nickname,
        String bio,
        String profileImageUrl,
        boolean leader
) {
}