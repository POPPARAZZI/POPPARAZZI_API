package com.spoons.popparazzi.moim.dto.request;

import com.spoons.popparazzi.moim.dto.command.UpdateMoimCommand;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateMoimRequest(

        @NotBlank(message = "모임 제목은 필수입니다.")
        @Size(max = 30, message = "모임 제목은 30자 이하여야 합니다.")
        String title,

        @NotBlank(message = "모임 소개는 필수입니다.")
        @Size(max = 200, message = "모임 소개는 200자 이하여야 합니다.")
        String content,

        @NotBlank(message = "사전 질문은 필수입니다.")
        @Size(max = 200, message = "사전 질문은 200자 이하여야 합니다.")
        String question,

        @Min(value = 2, message = "참여 인원은 최소 2명 이상이어야 합니다.")
        @Max(value = 9, message = "참여 인원은 최대 9명까지 가능합니다.")
        Integer maxParticipants,

        @NotEmpty(message = "카테고리는 최소 1개 이상 선택해야 합니다.")
        @Size(max = 3, message = "카테고리는 최대 3개까지 선택할 수 있습니다.")
        List<String> categoryCodes,

        List<Long> keepFileSeqs
) {
        public UpdateMoimCommand toCommand(String moimCode) {
                return new UpdateMoimCommand(
                        moimCode,
                        maxParticipants,
                        question,
                        categoryCodes,
                        title,
                        content,
                        keepFileSeqs == null ? List.of() : keepFileSeqs
                );
        }
}