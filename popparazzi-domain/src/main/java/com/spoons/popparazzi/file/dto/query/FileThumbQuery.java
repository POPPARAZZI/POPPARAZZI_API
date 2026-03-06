package com.spoons.popparazzi.file.dto.query;

public record FileThumbQuery(
        String parentCode,
        Long fileSeq,
        String url
) {}
