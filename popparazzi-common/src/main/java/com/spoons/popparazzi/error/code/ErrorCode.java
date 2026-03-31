package com.spoons.popparazzi.error.code;

import java.util.HashMap;

/**
 * 에러 코드 인터페이스
 *
 * <p>앱 API 규칙:</p>
 * <ul>
 *   <li>code: 앱에서 에러 구분용 (예: -1000, -2000)</li>
 *   <li>message: 사용자에게 보여줄 메시지</li>
 *   <li>HTTP 상태 코드는 사용하지 않음 (항상 200 OK)</li>
 * </ul>
 *
 * <p>코드 네이밍 규칙:</p>
 * <pre>
 * -1000 대 : Common (공통)
 * -2000 대 : Moim (모임)
 * </pre>
 */
public interface ErrorCode {

    int getCode();
    String getMessage();
    String getType();

}
