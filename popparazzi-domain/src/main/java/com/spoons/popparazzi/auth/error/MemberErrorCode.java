package com.spoons.popparazzi.auth.error;

import com.spoons.popparazzi.error.code.ErrorCode;

public enum MemberErrorCode implements ErrorCode {

    ALREADY_SIGNUP(-5001, "이미 가입되어 있는 유저입니다.", " MEMBER");

    private final int code;
    private final String message;
    private final String type;

    MemberErrorCode(int code, String message, String type) {
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
