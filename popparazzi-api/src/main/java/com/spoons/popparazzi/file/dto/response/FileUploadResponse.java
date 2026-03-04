package com.spoons.popparazzi.file.dto.response;

import java.util.List;

public record FileUploadResponse(
        List<Long> fileSeqs
) {
}