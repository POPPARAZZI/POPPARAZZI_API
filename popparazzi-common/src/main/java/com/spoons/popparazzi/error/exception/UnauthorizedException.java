package com.spoons.popparazzi.error.exception;

import com.spoons.popparazzi.error.code.ErrorCode;

/**
 * 인증(Authentication)이 필요하거나 실패했을 때 발생하는 예외
 *
 * <p>앱 API에서의 인증 처리:</p>
 * <ul>
 *   <li>HTTP 401이 아닌 200 OK + code로 구분</li>
 *   <li>앱은 code를 보고 로그인 화면으로 이동</li>
 * </ul>
 *
 * <p>사용 예시:</p>
 * <pre>
 * // JWT 필터에서
 * if (token == null) {
 *     throw new UnauthorizedException(CommonErrorCode.TOKEN_NOT_FOUND);
 * }
 *
 * if (jwtProvider.isExpired(token)) {
 *     throw new UnauthorizedException(CommonErrorCode.TOKEN_EXPIRED);
 * }
 *
 * // 앱 응답 (HTTP 200 OK):
 * {
 *   "code": -1010,
 *   "message": "토큰이 만료되었습니다",
 *   "data": null
 * }
 *
 * </pre>
 */
public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(ErrorCode errorCode) {
        super(errorCode);
    }

    public UnauthorizedException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
