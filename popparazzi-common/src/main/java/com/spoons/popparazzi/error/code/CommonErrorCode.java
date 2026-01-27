package com.spoons.popparazzi.error.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 공통 에러 코드
 *
 * <p>앱 API 공통 에러 정의</p>
 * <ul>
 *   <li>모든 도메인에서 공통으로 사용</li>
 * </ul>
 */
@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    // 400 Bad Request
    INVALID_INPUT_VALUE(-1000, "입력값이 올바르지 않습니다.", "COMMON"),
    INVALID_TYPE_VALUE(-1001, "입력 타입이 올바르지 않습니다.", "COMMON"),
    MISSING_INPUT_VALUE(-1002, "필수 입력값이 누락되었습니다.", "COMMON"),

    // 401 Unauthorized
    UNAUTHORIZED(-1010, "인증이 필요합니다.", "AUTH"),
    TOKEN_NOT_FOUND(-1011, "토큰이 존재하지 않습니다", "AUTH"),
    TOKEN_EXPIRED(-1012, "토큰이 만료되었습니다", "AUTH"),
    INVALID_TOKEN(-1013, "유효하지 않은 토큰입니다", "AUTH"),

    // 403 Forbidden
    FORBIDDEN(-1020, "접근 권한이 없습니다.", "COMMON"),

    // 404 Not Found
    RESOURCE_NOT_FOUND(-1030, "요청한 리소스를 찾을 수 없습니다.", "COMMON"),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(-1040, "서버 내부 오류가 발생했습니다.", "COMMON"),
    DATABASE_ERROR(-1041, "데이터베이스 오류가 발생했습니다.", "COMMON");

    private final int code;
    private final String message;
    private final String type;

}
