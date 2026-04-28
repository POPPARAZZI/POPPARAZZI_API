package com.spoons.popparazzi.moim.dto.result;

import com.spoons.popparazzi.file.dto.query.FileDetailQuery;

public record MoimDetailImageResult(
        Long fileSeq,
        String url
) {
    public static MoimDetailImageResult from(FileDetailQuery query) {
        return new MoimDetailImageResult(query.fileSeq(), query.url());
    }
}
