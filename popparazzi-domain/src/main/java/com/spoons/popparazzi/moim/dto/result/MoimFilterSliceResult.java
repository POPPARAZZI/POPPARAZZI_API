package com.spoons.popparazzi.moim.dto.result;

import java.util.List;

public record MoimFilterSliceResult(
        List<MoimFilterResult> content,
        int page,
        int size,
        boolean hasNext
) {
}