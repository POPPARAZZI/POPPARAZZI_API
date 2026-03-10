package com.spoons.popparazzi.moim.repository;

import com.spoons.popparazzi.common.YesNo;
import com.spoons.popparazzi.moim.entity.MoimMemberMapping;
import com.spoons.popparazzi.moim.entity.MoimMemberMappingId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MoimMemberMappingRepository extends JpaRepository<MoimMemberMapping, MoimMemberMappingId> {
    // 모임 참여 멤버 카운트
    long countByIdMoimCodeAndIsApprovedTrueAndJoinYn(String moimCode, YesNo joinYn);

    // 중복 신청 체크
    boolean existsByIdMoimCodeAndIdMemberCodeAndJoinYn(String moimCode, String memberCode, YesNo joinYn);
}