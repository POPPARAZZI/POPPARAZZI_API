package com.spoons.popparazzi.moim.dto.response;

import java.util.List;

public record MoimFilterSliceResponse(
        List<MoimFilterItemResponse> content,
        int page,
        int size,
        boolean hasNext
) {
}