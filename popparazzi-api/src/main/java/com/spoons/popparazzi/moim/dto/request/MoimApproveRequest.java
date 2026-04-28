package com.spoons.popparazzi.moim.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MoimApproveRequest {

    @NotBlank
    private String applicantMemberCode;
}