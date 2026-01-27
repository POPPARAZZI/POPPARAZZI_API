package com.spoons.popparazzi.error.exception;

import com.spoons.popparazzi.error.code.ErrorCode;

/**
 * 비즈니스 규칙을 위반한 잘못된 값이 입력되었을 때 발생하는 예외
 *
 * <p>@Valid와의 차이:</p>
 * <ul>
 *   <li>@Valid: 형식 검증 (null, 길이, 패턴) → Spring 자동 처리</li>
 *   <li>InvalidValueException: 비즈니스 로직 검증 → 수동 throw</li>
 * </ul>
 *
 * <p>사용 예시:</p>
 * <pre>
 * if (moim.isFull()) {
 *     throw new InvalidValueException(MoimErrorCode.MOIM_CAPACITY_EXCEEDED);
 * }
 *
 * // 앱 응답 (HTTP 200 OK):
 * {
 *   "code": -2001,
 *   "message": "모임 정원이 초과되었습니다",
 *   "data": null
 * }
 * </pre>
 */
public class InvalidValueException extends BusinessException {

    public InvalidValueException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidValueException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
