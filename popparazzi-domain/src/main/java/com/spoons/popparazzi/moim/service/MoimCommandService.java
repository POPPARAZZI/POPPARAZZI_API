package com.spoons.popparazzi.moim.service;

import com.spoons.popparazzi.moim.dto.command.CreateMoimCommand;

public interface MoimCommandService {

    /**
     * 모임 생성
     * @param command 생성 입력값(도메인 커맨드)
     * @param leaderMemberCode 로그인 유저(방장) 코드
     * @return 생성된 모임 코드(mm_code)
     */
    String create(CreateMoimCommand command, String leaderMemberCode);
}