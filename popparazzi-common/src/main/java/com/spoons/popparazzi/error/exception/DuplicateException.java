package com.spoons.popparazzi.error.exception;

import com.spoons.popparazzi.error.code.ErrorCode;

/**
 * 중복된 데이터가 존재할 때 발생하는 예외
 *
 * <p>사용 예시:</p>
 * <pre>
 * if (userRepository.existsByEmail(email)) {
 *     throw new DuplicateException(UserErrorCode.DUPLICATE_EMAIL);
 * }
 *
 * // 앱 응답 (HTTP 200 OK):
 * {
 *   "code": -3000,
 *   "message": "이미 사용중인 이메일입니다",
 *   "data": null
 * }
 * </pre>
 */
public class DuplicateException extends BusinessException{

    public DuplicateException(ErrorCode errorCode) {
        super(errorCode);
    }

    public DuplicateException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
