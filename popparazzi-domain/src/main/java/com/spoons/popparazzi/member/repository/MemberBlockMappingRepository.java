package com.spoons.popparazzi.member.repository;

import com.spoons.popparazzi.common.YesNo;
import com.spoons.popparazzi.member.entity.MemberBlockMapping;
import com.spoons.popparazzi.member.entity.MemberBlockMappingId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberBlockMappingRepository extends JpaRepository<MemberBlockMapping, MemberBlockMappingId> {

    // 1. 차단 여부 확인
    boolean existsByIdBlockerCodeAndIdBlockedCodeAndDeleteYn(String blockerCode, String blockedCode, YesNo deleteYn);

    // 2. 차단/차단 해제
    Optional<MemberBlockMapping> findByIdBlockerCodeAndIdBlockedCode(String blockerCode, String blockedCode);
}