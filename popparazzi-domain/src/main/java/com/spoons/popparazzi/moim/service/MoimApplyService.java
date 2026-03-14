package com.spoons.popparazzi.moim.service;

import com.spoons.popparazzi.moim.dto.command.ApplyMoimCommand;
import com.spoons.popparazzi.moim.dto.result.MoimApplyInfoResult;
import com.spoons.popparazzi.moim.dto.result.MoimParticipantsResult;

public interface MoimApplyService {

    // 1. 모임 참여 화면 조회
    MoimApplyInfoResult getApplyInfo(String moimCode, String memberCode);

    // 2. 모임 참여 신청
    void apply(String moimCode, String memberCode, ApplyMoimCommand command);

    // 3. 모임 참여 멤버 조회
    MoimParticipantsResult getParticipants(String moimCode, String memberCode);

}