package com.spoons.popparazzi.moim.dto.request;

import com.spoons.popparazzi.moim.dto.command.ApplyMoimCommand;
import jakarta.validation.constraints.NotBlank;

public record ApplyMoimRequest(

        @NotBlank(message = "모임 신청 답변은 필수입니다.")
        String answer
) {
        public ApplyMoimCommand toCommand() {
                return new ApplyMoimCommand(answer);
        }
}
