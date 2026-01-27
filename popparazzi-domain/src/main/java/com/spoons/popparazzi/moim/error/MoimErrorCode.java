package com.spoons.popparazzi.moim.error;

import com.spoons.popparazzi.error.code.ErrorCode;

public enum MoimErrorCode implements ErrorCode {

    MOIM_NOT_FOUND(-2000, "모임을 찾을 수 없습니다.", "MOIM"),
    MOIM_FULL(-2001, "모임 정원이 초과되었습니다.", " MOIM");

    private final int code;
    private final String message;
    private final String type;

    MoimErrorCode(int code, String message, String type) {
        this.code = code;
        this.message = message;
        this.type = type;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getType() {return type;}

}
