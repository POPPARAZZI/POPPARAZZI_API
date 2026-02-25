package com.spoons.popparazzi.moim.dto.query.recommend;

public record PreferredCategoryQuery(
        String categoryCode,
        long likeCount
) {
}
