package com.spoons.popparazzi.moim.dto.command;

import com.spoons.popparazzi.util.PaginationInfo;

public record MoimSearchCommand(
        String memberCode,
        String keyword,
        PaginationInfo paginationInfo
) {
}