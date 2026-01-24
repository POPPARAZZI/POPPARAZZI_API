package com.spoons.popparazzi.moim.dto.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CreateMoimCommand {

    private String title;


    public CreateMoimCommand(String title) {
        this.title = title;

    }

}
