package com.spoons.popparazzi.moim.service;

import com.spoons.popparazzi.common.YesNo;
import com.spoons.popparazzi.error.exception.BusinessException;
import com.spoons.popparazzi.moim.dto.command.ApplyMoimCommand;
import com.spoons.popparazzi.moim.entity.Moim;
import com.spoons.popparazzi.moim.entity.MoimMemberMapping;
import com.spoons.popparazzi.moim.error.MoimErrorCode;
import com.spoons.popparazzi.moim.repository.MoimMemberMappingRepository;
import com.spoons.popparazzi.moim.repository.MoimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class MoimApplyService {

    private final MoimRepository moimRepository;
    private final MoimMemberMappingRepository moimMemberMappingRepository;

    public void apply(String moimCode, String memberCode, ApplyMoimCommand command) {
        Moim moim = getMoim(moimCode);

        validateDeletedMoim(moim);
        validateNotLeader(moim, memberCode);
        validateMoimDate(moim);
        validateDuplicateApply(moimCode, memberCode);
        validateCapacity(moim);

        MoimMemberMapping mapping =
                MoimMemberMapping.applicant(moimCode, memberCode, command.answer());

        moimMemberMappingRepository.save(mapping);
    }

    private Moim getMoim(String moimCode) {
        return moimRepository.findByMoimCode(moimCode)
                .orElseThrow(() -> new BusinessException(MoimErrorCode.MOIM_NOT_FOUND));
    }

    private void validateDeletedMoim(Moim moim) {
        if (moim.getDeleteYn() != null && moim.getDeleteYn().isYes()) {
            throw new BusinessException(MoimErrorCode.MOIM_ALREADY_DELETED);
        }
    }

    private void validateNotLeader(Moim moim, String memberCode) {
        if (moim.getLeaderMemberCode().equals(memberCode)) {
            throw new BusinessException(MoimErrorCode.MOIM_APPLY_FORBIDDEN_TO_LEADER);
        }
    }

    private void validateMoimDate(Moim moim) {
        LocalDateTime now = LocalDateTime.now();

        if (moim.getDate().isBefore(now)) {
            throw new BusinessException(MoimErrorCode.MOIM_APPLY_CLOSED);
        }
    }

    private void validateDuplicateApply(String moimCode, String memberCode) {
        boolean alreadyApplied = moimMemberMappingRepository
                .existsByIdMoimCodeAndIdMemberCodeAndJoinYn(moimCode, memberCode, YesNo.YES);

        if (alreadyApplied) {
            throw new BusinessException(MoimErrorCode.MOIM_ALREADY_APPLIED);
        }
    }

    private void validateCapacity(Moim moim) {
        long approvedCount = moimMemberMappingRepository
                .countByIdMoimCodeAndIsApprovedTrueAndJoinYn(moim.getMoimCode(), YesNo.YES);

        if (approvedCount >= moim.getMaxParticipants()) {
            throw new BusinessException(MoimErrorCode.MOIM_FULL);
        }
    }
}