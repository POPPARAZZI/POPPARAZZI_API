package com.spoons.popparazzi.file.error;

import com.spoons.popparazzi.error.code.ErrorCode;

public enum FileErrorCode implements ErrorCode {

    FILE_EMPTY(-3000, "업로드할 파일이 없습니다.", "FILE"),
    FILE_TOO_MANY(-3001, "파일은 최대 5개까지 업로드 가능합니다.", "FILE"),
    FILE_NOT_FOUND(-3002, "파일을 찾을 수 없습니다.", "FILE"),
    FILE_ATTACH_FAILED(-3003, "파일 연결에 실패했습니다.", "FILE"),
    FILE_INVALID_PARENT(-3004, "잘못된 부모 코드입니다.", "FILE"),
    FILE_INVALID_TYPE(-3005,"지원하지 않는 파일 타입입니다.","FILE"),
    FILE_FORBIDDEN(-3006, "해당 파일에 대한 권한이 없습니다.", "FILE");

    private final int code;
    private final String message;
    private final String type;

    FileErrorCode(int code, String message, String type) {
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
    public String getType() {
        return type;
    }
}