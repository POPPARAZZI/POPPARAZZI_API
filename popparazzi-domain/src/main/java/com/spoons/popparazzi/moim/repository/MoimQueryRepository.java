package com.spoons.popparazzi.moim.repository;

import com.spoons.popparazzi.moim.dto.query.MoimApplyInfoQuery;
import com.spoons.popparazzi.moim.dto.query.MoimDetailQuery;
import com.spoons.popparazzi.moim.dto.query.MoimParticipantQuery;

import java.util.List;
import java.util.Optional;

public interface MoimQueryRepository {

    // 1. 모임 상세 조회
    MoimDetailQuery findMoimDetail(String moimCode);

    // 2. 모임 신청 화면 조회
    Optional<MoimApplyInfoQuery> findApplyInfoByMoimCode(String moimCode);

    // 3. 모임 참여자 조회
    List<MoimParticipantQuery> findParticipantsByMoimCode(String moimCode);
}
