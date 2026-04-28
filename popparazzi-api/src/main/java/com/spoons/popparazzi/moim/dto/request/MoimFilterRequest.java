package com.spoons.popparazzi.moim.dto.request;

import com.spoons.popparazzi.moim.dto.command.MoimFilterCommand;
import com.spoons.popparazzi.moim.enums.MoimViewType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MoimFilterRequest {

    @NotNull(message = "뷰 타입은 필수입니다.")
    private MoimViewType viewType;

    private String sido;
    private String sigungu;
    private LocalDate date;
    private List<String> categoryCodes;

    private int page = 0;
    private int size = 10;

    public MoimFilterCommand toCommand(String memberCode) {
        return MoimFilterCommand.builder()
                .memberCode(memberCode)
                .viewType(viewType)
                .sido(sido)
                .sigungu(sigungu)
                .date(date)
                .categoryCodes(categoryCodes)
                .page(page)
                .size(size)
                .build();
    }
}
