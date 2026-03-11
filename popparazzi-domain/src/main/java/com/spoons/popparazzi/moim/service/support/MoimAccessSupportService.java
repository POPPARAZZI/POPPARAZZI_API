package com.spoons.popparazzi.moim.service.support;

import com.spoons.popparazzi.error.exception.BusinessException;
import com.spoons.popparazzi.member.service.support.MemberBlockSupportService;
import com.spoons.popparazzi.moim.entity.Moim;
import com.spoons.popparazzi.moim.error.MoimErrorCode;
import com.spoons.popparazzi.moim.repository.MoimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MoimAccessSupportService {

    private final MoimRepository moimRepository;
    private final MemberBlockSupportService memberBlockSupportService;

    public Moim getAccessibleMoim(String moimCode, String memberCode) {
        Moim moim = moimRepository.findByMoimCode(moimCode)
                .orElseThrow(() -> new BusinessException(MoimErrorCode.MOIM_NOT_FOUND));

        validateNotDeleted(moim);
        validateNotBlocked(memberCode, moim.getLeaderMemberCode());

        return moim;
    }

    private void validateNotDeleted(Moim moim) {
        if (moim.getDeleteYn() != null && moim.getDeleteYn().isYes()) {
            throw new BusinessException(MoimErrorCode.MOIM_NOT_FOUND);
        }
    }

    private void validateNotBlocked(String memberCode, String leaderMemberCode) {
        boolean blocked = memberBlockSupportService.isBlockedBetween(memberCode, leaderMemberCode);

        if (blocked) {
            throw new BusinessException(MoimErrorCode.MOIM_NOT_FOUND);
        }
    }
}