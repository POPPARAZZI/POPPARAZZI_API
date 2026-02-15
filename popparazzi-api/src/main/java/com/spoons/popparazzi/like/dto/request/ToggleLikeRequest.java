package com.spoons.popparazzi.like.dto.request;

import com.spoons.popparazzi.like.entity.LikeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ToggleLikeRequest(
        @NotNull LikeType type,
        @NotBlank String targetCode
) {
}
