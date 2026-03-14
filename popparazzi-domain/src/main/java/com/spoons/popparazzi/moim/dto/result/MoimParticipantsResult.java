package com.spoons.popparazzi.moim.dto.result;

import com.spoons.popparazzi.moim.dto.query.MoimParticipantQuery;

import java.util.List;

public record MoimParticipantsResult(
        List<MoimParticipantResult> participants
) {

    public static MoimParticipantsResult from(List<MoimParticipantQuery> queries) {
        return new MoimParticipantsResult(
                queries.stream()
                        .map(MoimParticipantResult::from)
                        .toList()
        );
    }
}