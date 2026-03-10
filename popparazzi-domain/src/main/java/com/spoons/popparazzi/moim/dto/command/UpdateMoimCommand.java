package com.spoons.popparazzi.moim.dto.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class UpdateMoimCommand {

    private final String moimCode;
    private final int maxParticipants;
    private final String preQuestion;
    private final List<String> categoryCodes;
    private final String title;
    private final String content;
    private final List<Long> keepFileSeqs;
}