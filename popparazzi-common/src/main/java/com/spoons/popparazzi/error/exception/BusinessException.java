package com.spoons.popparazzi.error.exception;

import com.spoons.popparazzi.error.code.ErrorCode;
import lombok.Getter;

/**
 * 비즈니스 로직 수행 중 발생하는 예외를 처리하기 위한 커스텀 예외 클래스
 *
 * <p>앱 API 특징:</p>
 * <ul>
 *   <li>HTTP 상태 코드는 항상 200 OK</li>
 *   <li>예외 발생 시 응답 body의 code로 에러 구분</li>
 *   <li>ErrorCode의 code 필드가 앱에서 에러 판단 기준</li>
 * </ul>
 *
 * <p>사용 시점:</p>
 * <ul>
 *   <li>도메인 규칙 위반: 모임 정원 초과, 중복 가입 시도 등</li>
 *   <li>데이터 검증 실패: 존재하지 않는 리소스 조회, 권한 없는 접근 등</li>
 *   <li>비즈니스 상태 오류: 이미 종료된 모임 수정 시도, 취소 불가능한 상태 등</li>
 * </ul>
 *
 * <p>사용 예시:</p>
 * <pre>
 * // 1. 리소스를 찾을 수 없을 때
 * Moim moim = moimRepository.findById(moimId)
 *     .orElseThrow(() -> new EntityNotFoundException(MoimErrorCode.MOIM_NOT_FOUND));
 *
 * // 앱 응답:
 * {
 *   "code": -2000,
 *   "message": "모임을 찾을 수 없습니다",
 *   "data": null
 * }
 *
 * // 2. 비즈니스 규칙 위반 시
 * if (moim.isFull()) {
 *     throw new InvalidValueException(MoimErrorCode.MOIM_CAPACITY_EXCEEDED);
 * }
 *
 * // 앱 응답:
 * {
 *   "code": -2000,
 *   "message": "모임 정원이 초과되었습니다",
 *   "data": null
 * }
 *
 * // 3. 권한 검증 실패 시
 * if (!moim.isOwner(userId)) {
 *     throw new ForbiddenException(MoimErrorCode.NOT_MOIM_OWNER);
 * }
 *
 * // 앱 응답:
 * {
 *   "code": -3000,
 *   "message": "모임 소유자만 수정할 수 있습니다",
 *   "data": null
 * }
 * </pre>
 *
 * <p>주의사항:</p>
 * <ul>
 *   <li>시스템 예외(DB 연결 실패, NPE 등)에는 사용하지 않음</li>
 *   <li>Validation 예외(@Valid)는 Spring이 자동 처리</li>
 *   <li>반드시 ErrorCode와 함께 사용</li>
 * </ul>
 */
@Getter
public class BusinessException extends RuntimeException{

    private final ErrorCode errorCode;

    /**
     * ErrorCode만으로 예외 생성
     *
     * @param errorCode 에러 코드 (메시지는 ErrorCode에 정의된 기본 메시지 사용)
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * ErrorCode + 커스텀 메시지로 예외 생성
     * 기본 메시지에 추가 정보를 포함하고 싶을 때 사용
     *
     * @param errorCode 에러 코드
     * @param message 커스텀 메시지 (예: "모임 ID: 123을 찾을 수 없습니다")
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * ErrorCode + 원인 예외로 예외 생성
     * 다른 예외를 래핑할 때 사용
     *
     * @param errorCode 에러 코드
     * @param cause 원인 예외
     */
    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
}
