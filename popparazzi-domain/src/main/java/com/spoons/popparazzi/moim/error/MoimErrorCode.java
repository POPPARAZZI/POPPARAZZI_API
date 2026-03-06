package com.spoons.popparazzi.moim.error;

import com.spoons.popparazzi.error.code.ErrorCode;

public enum MoimErrorCode implements ErrorCode {

    MOIM_NOT_FOUND(-2000, "모임을 찾을 수 없습니다.", "MOIM"),
    MOIM_FULL(-2001, "모임 정원이 초과되었습니다.", " MOIM"),
    INVALID_REQUEST(-2002,"잘못된 요청입니다.","MOIM"),
    MOIM_DELETE_FORBIDDEN(-2003, "모임 작성자만 삭제할 수 있습니다.", "MOIM"),
    MOIM_DELETE_NOT_ALLOWED_ON_EVENT_DAY(-2004, "모임 당일에는 삭제할 수 없습니다.", "MOIM"),
    MOIM_ALREADY_DELETED(-2005, "이미 삭제된 모임입니다.", "MOIM");

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
