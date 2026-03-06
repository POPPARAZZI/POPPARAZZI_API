package com.spoons.popparazzi.moim.dto.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class CreateMoimCommand {

    private final String popupCode;
    private final int maxParticipants;
    private final LocalDateTime scheduleAt;
    private final String preQuestion;
    private final List<String> categoryCodes;
    private final String title;
    private final String content;
}