package com.spoons.popparazzi.moim.dto.command;

import com.spoons.popparazzi.moim.enums.MoimViewType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class MoimFilterCommand {
    // 멤버 코드
    private String memberCode;

    // 탭 (FAVORITE / HOT / NEW)
    private MoimViewType viewType;

    // 지역 - 상위 (서울, 경기 등)
    private String sido;

    // 지역 - 하위 (강남구, 성수 등)
    private String sigungu;

    // 일정 (선택한 날짜)
    private LocalDate date;

    // 카테고리 코드 리스트 (다중 선택)
    private List<String> categoryCodes;

    // 페이징
    private int page;
    private int size;

    /**
     * 지역 필터가 적용되었는지 여부
     */
    public boolean hasRegion() {
        return sido != null && !sido.isBlank();
    }

    /**
     * 하위 지역까지 선택했는지 여부
     */
    public boolean hasSigungu() {
        return sigungu != null && !sigungu.isBlank();
    }

    /**
     * 날짜 필터 여부
     */
    public boolean hasDate() {
        return date != null;
    }

    /**
     * 카테고리 필터 여부
     */
    public boolean hasCategory() {
        return categoryCodes != null && !categoryCodes.isEmpty();
    }
}