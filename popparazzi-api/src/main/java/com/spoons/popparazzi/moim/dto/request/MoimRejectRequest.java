package com.spoons.popparazzi.moim.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MoimRejectRequest {

    @NotBlank
    private String applicantMemberCode;
}