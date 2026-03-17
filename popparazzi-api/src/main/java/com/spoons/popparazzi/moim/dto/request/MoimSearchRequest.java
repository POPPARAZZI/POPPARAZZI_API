package com.spoons.popparazzi.moim.dto.request;

import com.spoons.popparazzi.util.PaginationInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "모임 검색 요청")
public class MoimSearchRequest {

    @NotBlank(message = "검색어는 필수입니다.")
    @Schema(description = "검색어", example = "팝업, 모임명")
    private String keyword;

    @Valid
    @Schema(description = "페이징 정보")
    private PaginationInfo paginationInfo = defaultPagination();

    private PaginationInfo defaultPagination() {
        PaginationInfo paginationInfo = new PaginationInfo();
        paginationInfo.setCurrentPage(1);
        paginationInfo.setRecordCountPerPage(10);
        return paginationInfo;
    }
}