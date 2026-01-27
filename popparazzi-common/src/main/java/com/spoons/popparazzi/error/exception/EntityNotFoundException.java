package com.spoons.popparazzi.error.exception;

import com.spoons.popparazzi.error.code.ErrorCode;

/**
 * 데이터베이스에서 엔티티를 찾을 수 없을 때 발생하는 예외
 *
 * <p>사용 예시:</p>
 * <pre>
 * Moim moim = moimRepository.findById(moimId)
 *     .orElseThrow(() -> new EntityNotFoundException(MoimErrorCode.MOIM_NOT_FOUND));
 *
 * // 앱 응답 (HTTP 200 OK):
 * {
 *   "code": -1000,
 *   "message": "모임을 찾을 수 없습니다",
 *   "data": null
 * }
 * </pre>
 */
public class EntityNotFoundException extends BusinessException{

    public EntityNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public EntityNotFoundException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
