package com.spoons.popparazzi.moim.service;

import com.spoons.popparazzi.exception.BusinessException;
import com.spoons.popparazzi.moim.dto.command.CreateMoimCommand;
import com.spoons.popparazzi.moim.exception.MoimErrorCode;
import org.springframework.stereotype.Service;

@Service
public class MoimServiceImpl implements MoimService{
    @Override
    public Long create(CreateMoimCommand command) {

        if(command == null){
            throw new BusinessException(MoimErrorCode.MOIM_NOT_FOUND);
        }
        return null;
    }
}
