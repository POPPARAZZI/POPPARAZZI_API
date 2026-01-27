package com.spoons.popparazzi.error.exception;

import com.spoons.popparazzi.error.code.ErrorCode;

/**
 * 인가(Authorization)가 실패했을 때 발생하는 예외
 *
 * <p>사용 예시:</p>
 * <pre>
 * if (!moim.isOwner(userId)) {
 *     throw new ForbiddenException(MoimErrorCode.NOT_MOIM_OWNER);
 * }
 *
 * // 앱 응답 (HTTP 200 OK):
 * {
 *   "code": -2003,
 *   "message": "모임 소유자만 수정할 수 있습니다",
 *   "data": null
 * }
 * </pre>
 */
public class ForbiddenException extends BusinessException {

    public ForbiddenException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ForbiddenException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
