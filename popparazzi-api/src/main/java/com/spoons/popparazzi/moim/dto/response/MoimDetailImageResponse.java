package com.spoons.popparazzi.moim.dto.response;

import com.spoons.popparazzi.moim.dto.result.MoimDetailImageResult;

public record MoimDetailImageResponse(
        Long fileSeq,
        String url
) {
    public static MoimDetailImageResponse from(MoimDetailImageResult result) {
        return new MoimDetailImageResponse(
                result.fileSeq(),
                result.url()
        );
    }
}
