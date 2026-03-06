package com.spoons.popparazzi.moim.dto.request;

import com.spoons.popparazzi.moim.dto.command.CreateMoimCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class CreateMoimRequest {

    @NotBlank
    private String popupCode;

    @NotNull
    private Integer maxParticipants;

    @NotNull
    private LocalDateTime scheduleAt;

    @NotBlank
    private String preQuestion;

    @NotNull
    private List<String> categoryCodes;

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    public CreateMoimCommand toCommand() {
        return new CreateMoimCommand(
                popupCode,
                maxParticipants,
                scheduleAt,
                preQuestion,
                categoryCodes,
                title,
                content
        );
    }
}