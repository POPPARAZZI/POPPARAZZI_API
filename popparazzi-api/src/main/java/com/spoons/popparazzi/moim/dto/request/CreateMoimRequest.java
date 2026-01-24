package com.spoons.popparazzi.moim.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.spoons.popparazzi.moim.dto.command.CreateMoimCommand;

@Getter
@NoArgsConstructor
public class CreateMoimRequest {

    @NotBlank
    private String title;

    public CreateMoimCommand toCommand() {
        return new CreateMoimCommand(
                title
        );
    }
}
