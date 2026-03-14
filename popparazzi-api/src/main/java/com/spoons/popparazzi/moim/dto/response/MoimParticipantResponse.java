package com.spoons.popparazzi.moim.dto.response;

import com.spoons.popparazzi.moim.dto.result.MoimParticipantResult;

public record MoimParticipantResponse(
        String memberCode,
        String nickname,
        String bio,
        String profileImageUrl,
        boolean leader
) {

    public static MoimParticipantResponse from(MoimParticipantResult result) {
        return new MoimParticipantResponse(
                result.memberCode(),
                result.nickname(),
                result.bio(),
                result.profileImageUrl(),
                result.leader()
        );
    }
}