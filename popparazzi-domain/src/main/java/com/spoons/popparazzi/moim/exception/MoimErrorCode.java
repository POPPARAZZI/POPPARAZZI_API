package com.spoons.popparazzi.moim.exception;

import com.spoons.popparazzi.exception.ErrorCode;

public enum MoimErrorCode implements ErrorCode {

    MOIM_NOT_FOUND("MOIM_001", "모임을 찾을 수 없습니다."),
    MOIM_FULL("MOIM_002", "모임 정원이 초과되었습니다.");

    private final String code;
    private final String message;

    MoimErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
