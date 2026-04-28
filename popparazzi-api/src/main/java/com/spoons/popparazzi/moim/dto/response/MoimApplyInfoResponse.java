package com.spoons.popparazzi.moim.dto.response;

import com.spoons.popparazzi.moim.dto.result.MoimApplyInfoResult;

public record MoimApplyInfoResponse(
        String leaderProfileImageUrl,
        String leaderNickname,
        String question
) {
    public static MoimApplyInfoResponse from(MoimApplyInfoResult result) {
        return new MoimApplyInfoResponse(
                result.leaderProfileImageUrl(),
                result.leaderNickname(),
                result.question()
        );
    }
}
