package com.spoons.popparazzi.moim.dto.response;

import com.spoons.popparazzi.moim.dto.result.MoimFilterSliceResult;

import java.util.List;

public record MoimFilterSliceResponse(
        List<MoimFilterItemResponse> content,
        int page,
        int size,
        boolean hasNext
) {
    public static MoimFilterSliceResponse from(MoimFilterSliceResult result) {
        List<MoimFilterItemResponse> content = result.content().stream()
                .map(MoimFilterItemResponse::from)
                .toList();

        return new MoimFilterSliceResponse(
                content,
                result.page(),
                result.size(),
                result.hasNext()
        );
    }
}
