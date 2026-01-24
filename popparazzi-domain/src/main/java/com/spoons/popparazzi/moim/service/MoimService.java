package com.spoons.popparazzi.moim.service;

import com.spoons.popparazzi.moim.dto.command.CreateMoimCommand;

public interface MoimService {
    Long create(CreateMoimCommand command);
}
