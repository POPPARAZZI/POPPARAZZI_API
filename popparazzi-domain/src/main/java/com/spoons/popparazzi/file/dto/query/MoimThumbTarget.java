package com.spoons.popparazzi.file.dto.query;

/**
 * 모임 썸네일 조회를 위한 입력 DTO
 * - 모임 썸네일이 없으면 popupCode의 썸네일로 fallback 한다.
 */
public record MoimThumbTarget(
        String moimCode,
        String popupCode
) {
}