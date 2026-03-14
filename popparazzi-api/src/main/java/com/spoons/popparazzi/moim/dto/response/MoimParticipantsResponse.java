package com.spoons.popparazzi.moim.dto.response;

import com.spoons.popparazzi.moim.dto.result.MoimParticipantsResult;

public record MoimParticipantsResponse(
        java.util.List<MoimParticipantResponse> participants
) {

    public static MoimParticipantsResponse from(MoimParticipantsResult result) {
        return new MoimParticipantsResponse(
                result.participants().stream()
                        .map(MoimParticipantResponse::from)
                        .toList()
        );
    }
}