package com.spoons.popparazzi.moim.dto.result;

import com.spoons.popparazzi.moim.dto.query.MoimParticipantQuery;

public record MoimParticipantResult(
        String memberCode,
        String nickname,
        String bio,
        String profileImageUrl,
        boolean leader
) {

    public static MoimParticipantResult from(MoimParticipantQuery query) {
        return new MoimParticipantResult(
                query.memberCode(),
                query.nickname(),
                query.bio(),
                query.profileImageUrl(),
                query.leader()
        );
    }
}