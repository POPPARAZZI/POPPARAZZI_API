package com.spoons.popparazzi.moim.dto.result;

public record MoimApplyInfoResult(
        String leaderProfileImageUrl,
        String leaderNickname,
        String question
) {
    public static MoimApplyInfoResult of(
            String leaderProfileImageUrl,
            String leaderNickname,
            String question
    ) {
        return new MoimApplyInfoResult(leaderProfileImageUrl, leaderNickname, question);
    }
}
