package com.spoons.popparazzi.moim.repository;

import com.spoons.popparazzi.moim.entity.MoimMemberMapping;
import com.spoons.popparazzi.moim.entity.MoimMemberMappingId;
import com.spoons.popparazzi.moim.enums.MoimMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface MoimMemberMappingRepository extends JpaRepository<MoimMemberMapping, MoimMemberMappingId> {

    // 모임 승인 완료 멤버 카운트
    long countByIdMoimCodeAndStatus(String moimCode, MoimMemberStatus status);

    // 중복 신청 체크
    boolean existsByIdMoimCodeAndIdMemberCodeAndStatusIn(
            String moimCode,
            String memberCode,
            Collection<MoimMemberStatus> statuses
    );

    // 특정 모임 신청 건 조회
    Optional<MoimMemberMapping> findByIdMoimCodeAndIdMemberCode(String moimCode, String memberCode);
}