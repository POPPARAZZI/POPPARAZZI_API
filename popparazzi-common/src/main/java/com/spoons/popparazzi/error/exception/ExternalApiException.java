package com.spoons.popparazzi.error.exception;

import com.spoons.popparazzi.error.code.ErrorCode;

/**
 * 외부 API 호출 중 발생한 오류를 처리하는 예외
 *
 * <p>사용 예시:</p>
 * <pre>
 * try {
 *     KakaoUserInfo userInfo = kakaoClient.getUserInfo(token);
 * } catch (Exception e) {
 *     throw new ExternalApiException(
 *         CommonErrorCode.EXTERNAL_API_ERROR,
 *         "카카오 API 호출 실패",
 *         e
 *     );
 * }
 *
 * // 앱 응답 (HTTP 200 OK):
 * {
 *   "code": -1005,
 *   "message": "외부 API 호출에 실패했습니다",
 *   "data": null
 * }
 * </pre>
 */
public class ExternalApiException extends BusinessException{

    public ExternalApiException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ExternalApiException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public ExternalApiException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public ExternalApiException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message);
        initCause(cause);
    }
}
