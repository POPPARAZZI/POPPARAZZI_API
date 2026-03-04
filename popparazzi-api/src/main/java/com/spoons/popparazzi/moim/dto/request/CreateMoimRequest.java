package com.spoons.popparazzi.moim.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.spoons.popparazzi.moim.dto.command.CreateMoimCommand;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class CreateMoimRequest {

    @NotBlank
    private String popupCode;
    private int maxParticipants;
    private LocalDateTime scheduleAt;
    private String preQuestion;
    private List<String> categoryCodes;

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private List<Long> fileSeqs;

    public CreateMoimCommand toCommand() {
        return new CreateMoimCommand(
                popupCode,
                maxParticipants,
                scheduleAt,
                preQuestion,
                categoryCodes,
                title,
                content,
                fileSeqs
        );
    }
}