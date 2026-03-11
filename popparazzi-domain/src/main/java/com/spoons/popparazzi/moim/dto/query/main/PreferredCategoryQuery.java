package com.spoons.popparazzi.moim.dto.query.main;

public record PreferredCategoryQuery(
        String categoryCode,
        long likeCount
) {
}
