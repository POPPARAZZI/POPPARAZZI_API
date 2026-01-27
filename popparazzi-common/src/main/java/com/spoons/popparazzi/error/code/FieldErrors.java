package com.spoons.popparazzi.error.code;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Validation 실패 시 필드별 에러 정보를 담는 객체
 */
@Getter
@Builder
public class FieldErrors {


    private final List<FieldError> fieldErrors;

    @Getter
    @Builder
    public static class FieldError {
        private final String field;
        private final String value;
        private final String reason;
    }
}
