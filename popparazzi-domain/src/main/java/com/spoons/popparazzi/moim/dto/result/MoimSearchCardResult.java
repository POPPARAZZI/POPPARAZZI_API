package com.spoons.popparazzi.moim.dto.result;

import java.util.List;

public record MoimSearchCardResult(
        String moimCode,
        String thumbnailUrl,
        boolean liked,
        List<String> categories,
        String title,
        String address,
        List<String> participantProfileUrls,
        String leaderNickname,
        int currentCount,
        int maxCount,
        boolean closingSoon
) {
}