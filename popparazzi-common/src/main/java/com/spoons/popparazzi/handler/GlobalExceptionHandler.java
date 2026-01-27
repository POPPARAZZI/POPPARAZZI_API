package com.spoons.popparazzi.handler;

import com.spoons.popparazzi.error.code.FieldErrors;
import com.spoons.popparazzi.error.exception.BusinessException;
import com.spoons.popparazzi.error.code.ErrorCode;
import com.spoons.popparazzi.error.code.CommonErrorCode;
import com.spoons.popparazzi.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 앱 API 전역 예외 처리 핸들러
 *
 * <p>핵심 원칙:</p>
 * <ul>
 *   <li>모든 예외는 HTTP 200 OK로 응답</li>
 *   <li>응답 body의 code로 성공/실패 구분</li>
 *   <li>앱은 code 값으로 에러 처리 분기</li>
 * </ul>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * BusinessException 및 하위 예외 처리
     * - EntityNotFoundException
     * - DuplicateException
     * - InvalidValueException
     * - UnauthorizedException
     * - ForbiddenException
     * - ExternalApiException
     */
    @ExceptionHandler(BusinessException.class)
    protected ApiResponse<Void> handleBusinessException(BusinessException e) {
        log.warn("BusinessException: code={}, message={}",
                e.getErrorCode().getCode(), e.getMessage());

        return ApiResponse.error(e.getErrorCode());
    }

    /**
     * @Valid 검증 실패 처리 (@RequestBody)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ApiResponse<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("MethodArgumentNotValidException: {}", e.getMessage());

        List<FieldErrors.FieldError> fieldErrors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> FieldErrors.FieldError.builder()
                        .field(error.getField())
                        .value(error.getRejectedValue() != null ?
                                error.getRejectedValue().toString() : "")
                        .reason(error.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());

        FieldErrors errors = FieldErrors.builder()
                .fieldErrors(fieldErrors)
                .build();

        return ApiResponse.error(CommonErrorCode.INVALID_INPUT_VALUE, errors);
    }

    /**
     * @Valid 검증 실패 처리 (@ModelAttribute)
     */
    @ExceptionHandler(BindException.class)
    protected ApiResponse<Void> handleBindException(BindException e) {
        log.warn("BindException: {}", e.getMessage());

        List<FieldErrors.FieldError> fieldErrors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> FieldErrors.FieldError.builder()
                        .field(error.getField())
                        .value(error.getRejectedValue() != null ?
                                error.getRejectedValue().toString() : "")
                        .reason(error.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());

        FieldErrors errors = FieldErrors.builder()
                .fieldErrors(fieldErrors)
                .build();

        return ApiResponse.error(CommonErrorCode.INVALID_INPUT_VALUE, errors);
    }

    /**
     * 타입 불일치 예외 처리
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ApiResponse<Void> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e) {
        log.warn("MethodArgumentTypeMismatchException: {}", e.getMessage());

        return ApiResponse.error(CommonErrorCode.INVALID_TYPE_VALUE);
    }

    /**
     * 그 외 모든 예외 처리
     *
     * <p>주의:</p>
     * <ul>
     *   <li>예상치 못한 시스템 오류</li>
     *   <li>반드시 로그 레벨 ERROR로 기록</li>
     *   <li>실제 에러 내용은 숨기고 공통 메시지만 노출</li>
     * </ul>
     */
    @ExceptionHandler(Exception.class)
    protected ApiResponse<Void> handleException(Exception e) {
        log.error("Unexpected Exception: ", e);

        return ApiResponse.error(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }
}
