package com.spoons.popparazzi.member.service.support;

import com.spoons.popparazzi.common.YesNo;
import com.spoons.popparazzi.member.repository.MemberBlockMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberBlockSupportServiceImpl implements MemberBlockSupportService {

    private final MemberBlockMappingRepository memberBlockMappingRepository;

    @Override
    public boolean isBlockedBetween(String memberCode, String targetMemberCode) {
        return isBlocked(memberCode, targetMemberCode)
                || isBlocked(targetMemberCode, memberCode);
    }

    private boolean isBlocked(String blockerCode, String blockedCode) {
        return memberBlockMappingRepository
                .existsByIdBlockerCodeAndIdBlockedCodeAndDeleteYn(
                        blockerCode,
                        blockedCode,
                        YesNo.NO
                );
    }
}