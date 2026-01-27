package com.spoons.popparazzi.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.spoons.popparazzi.error.code.ErrorCode;
import com.spoons.popparazzi.error.code.FieldErrors;
import lombok.Getter;

/**
 * 앱 API 전용 통합 응답 객체
 *
 * <p>앱 API 응답 규칙:</p>
 * <ul>
 *   <li>HTTP 상태 코드: 항상 200 OK</li>
 *   <li>성공/실패 구분: code 필드로 판단</li>
 *   <li>성공: code = 0</li>
 *   <li>실패: code = 비즈니스 에러 코드 (예: -1000, -2000)</li>
 * </ul>
 *
 * <p>응답 예시:</p>
 * <pre>
 * // 성공
 * {
 *   "code": 0,
 *   "message": "성공",
 *   "data": {...}
 * }
 *
 * // 실패
 * {
 *   "code": -2000,
 *   "message": "모임을 찾을 수 없습니다",
 *   "data": null
 * }
 *
 * // Validation 실패
 * {
 *   "code": -1000,
 *   "message": "입력값이 올바르지 않습니다",
 *   "data": null,
 *   "errors": [
 *     {
 *       "field": "email",
 *       "value": "invalid",
 *       "reason": "이메일 형식이 올바르지 않습니다"
 *     }
 *   ]
 * }
 * </pre>
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final int code;
    private final String message;
    private final T data;
    private final FieldErrors errors;

    private ApiResponse(int code, String message, T data, FieldErrors errors) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.errors = errors;
    }

    // ===== 성공 응답 =====

    /**
     * 데이터와 함께 성공 응답 생성
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "성공", data, null);
    }

    /**
     * 데이터와 커스텀 메시지로 성공 응답 생성
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(0, message, data, null);
    }

    /**
     * 데이터 없이 성공 응답 생성 (예: 삭제, 수정 완료)
     */
    public static ApiResponse<Void> success() {
        return new ApiResponse<>(0, "성공", null, null);
    }

    /**
     * 메시지만으로 성공 응답 생성
     */
    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(0, message, null, null);
    }

    // ===== 실패 응답 =====

    /**
     * ErrorCode로 실패 응답 생성
     */
    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return new ApiResponse<>(
                errorCode.getCode(),
                errorCode.getMessage(),
                null,
                null
        );
    }

    /**
     * ErrorCode와 커스텀 메시지로 실패 응답 생성
     */
    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message) {
        return new ApiResponse<>(
                errorCode.getCode(),
                message,
                null,
                null
        );
    }

    /**
     * Validation 실패 응답 생성 (필드 에러 포함)
     */
    public static <T> ApiResponse<T> error(ErrorCode errorCode, FieldErrors errors) {
        return new ApiResponse<>(
                errorCode.getCode(),
                errorCode.getMessage(),
                null,
                errors
        );
    }

    /**
     * 코드와 메시지로 직접 실패 응답 생성
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null, null);
    }
}
