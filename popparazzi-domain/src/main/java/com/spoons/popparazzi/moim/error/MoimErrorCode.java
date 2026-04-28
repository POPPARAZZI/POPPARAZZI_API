package com.spoons.popparazzi.moim.error;

import com.spoons.popparazzi.error.code.ErrorCode;

public enum MoimErrorCode implements ErrorCode {

    // =========================
    // 공통 / 조회
    // =========================
    MOIM_NOT_FOUND(-2000, "모임을 찾을 수 없습니다.", "MOIM"),
    INVALID_REQUEST(-2001, "잘못된 요청입니다.", "MOIM"),
    MOIM_ALREADY_DELETED(-2002, "이미 삭제된 모임입니다.", "MOIM"),

    // =========================
    // 모임 신청
    // =========================
    MOIM_APPLY_FORBIDDEN_TO_LEADER(-2003, "본인이 만든 모임에는 신청할 수 없습니다.", "MOIM"),
    MOIM_ALREADY_APPLIED(-2004, "이미 신청한 모임입니다.", "MOIM"),
    MOIM_FULL(-2005, "모임 정원이 초과되었습니다.", "MOIM"),
    MOIM_APPLY_CLOSED(-2006, "이미 지난 모임에는 신청할 수 없습니다.", "MOIM"),

    // =========================
    // 모임 수정
    // =========================
    MOIM_UPDATE_FORBIDDEN(-2007, "모임 작성자만 수정할 수 있습니다.", "MOIM"),

    // =========================
    // 모임 삭제
    // =========================
    MOIM_DELETE_FORBIDDEN(-2008, "모임 작성자만 삭제할 수 있습니다.", "MOIM"),
    MOIM_DELETE_NOT_ALLOWED_ON_EVENT_DAY(-2009, "모임 당일에는 삭제할 수 없습니다.", "MOIM"),

    // =========================
    // 모임 검색
    // =========================
    INVALID_SEARCH_KEYWORD(-2010, "잘못된 검색 키워드입니다.", "MOIM"),

    // =========================
    // 모임 승인 / 거절
    // =========================
    MOIM_APPROVE_REJECT_FORBIDDEN(-2011, "모임장만 승인/거절할 수 있습니다.", "MOIM"),
    MOIM_APPLY_NOT_FOUND(-2012, "모임 신청 내역을 찾을 수 없습니다.", "MOIM"),
    MOIM_APPLY_ALREADY_PROCESSED(-2013, "이미 처리된 모임 신청입니다.", "MOIM"),
    MOIM_MEMBER_BLOCKED(-2014, "차단 관계가 있는 회원은 승인할 수 없습니다.", "MOIM");

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
    public String getType() {
        return type;
    }
}