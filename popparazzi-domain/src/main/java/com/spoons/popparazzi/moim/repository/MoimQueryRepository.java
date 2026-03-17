package com.spoons.popparazzi.moim.repository;

import com.spoons.popparazzi.moim.dto.query.MoimApplyInfoQuery;
import com.spoons.popparazzi.moim.dto.query.MoimDetailQuery;
import com.spoons.popparazzi.moim.dto.query.MoimParticipantProfileQuery;
import com.spoons.popparazzi.moim.dto.query.MoimParticipantQuery;
import com.spoons.popparazzi.moim.dto.query.main.MoimParticipantsCountQuery;

import java.util.List;
import java.util.Optional;

public interface MoimQueryRepository {

    // 1. 모임 상세 조회
    MoimDetailQuery findMoimDetail(String moimCode);

    // 2. 모임 신청 화면 조회
    Optional<MoimApplyInfoQuery> findApplyInfoByMoimCode(String moimCode);

    // 3. 단일 모임 참여자 목록 조회
    List<MoimParticipantQuery> findParticipantsByMoimCode(String moimCode);

    // 4. 여러 모임의 승인 완료 참여자 수 집계
    List<MoimParticipantsCountQuery> countApprovedParticipants(List<String> moimCodes);

    // 5. 여러 모임의 참여자 프로필 URL 조회 (최대 2명)
    List<MoimParticipantProfileQuery> findParticipantProfiles(List<String> moimCodes);

}
