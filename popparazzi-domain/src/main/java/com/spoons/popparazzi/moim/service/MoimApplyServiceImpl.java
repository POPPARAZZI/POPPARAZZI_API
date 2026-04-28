package com.spoons.popparazzi.moim.service;

import com.spoons.popparazzi.common.YesNo;
import com.spoons.popparazzi.error.exception.BusinessException;
import com.spoons.popparazzi.member.repository.MemberBlockMappingRepository;
import com.spoons.popparazzi.moim.dto.command.ApplyMoimCommand;
import com.spoons.popparazzi.moim.dto.result.MoimApplyInfoResult;
import com.spoons.popparazzi.moim.dto.result.MoimParticipantsResult;
import com.spoons.popparazzi.moim.entity.Moim;
import com.spoons.popparazzi.moim.entity.MoimMemberMapping;
import com.spoons.popparazzi.moim.enums.MoimMemberStatus;
import com.spoons.popparazzi.moim.error.MoimErrorCode;
import com.spoons.popparazzi.moim.repository.MoimMemberMappingRepository;
import com.spoons.popparazzi.moim.repository.MoimQueryRepository;
import com.spoons.popparazzi.moim.service.support.MoimAccessSupportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MoimApplyServiceImpl implements MoimApplyService {

    private final MoimAccessSupportService moimAccessSupportService;
    private final MoimQueryRepository moimQueryRepository;
    private final MoimMemberMappingRepository moimMemberMappingRepository;
    private final MemberBlockMappingRepository memberBlockMappingRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // 모임 신청 화면 조회
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public MoimApplyInfoResult getApplyInfo(String moimCode, String memberCode) {
        moimAccessSupportService.getAccessibleMoim(moimCode, memberCode);

        return moimQueryRepository.findApplyInfoByMoimCode(moimCode)
                .orElseThrow(() -> new BusinessException(MoimErrorCode.MOIM_NOT_FOUND));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 모임 신청
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void apply(String moimCode, String memberCode, ApplyMoimCommand command) {
        Moim moim = moimAccessSupportService.getAccessibleMoim(moimCode, memberCode);

        validateNotLeader(moim, memberCode);
        validateMoimDate(moim);
        validateDuplicateApply(moimCode, memberCode);
        validateCapacity(moim);

        moimMemberMappingRepository.save(
                MoimMemberMapping.applicant(moimCode, memberCode, command.answer())
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 모임 참여자 조회
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public MoimParticipantsResult getParticipants(String moimCode, String memberCode) {
        moimAccessSupportService.getAccessibleMoim(moimCode, memberCode);

        return MoimParticipantsResult.from(
                moimQueryRepository.findParticipantsByMoimCode(moimCode)
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 모임 신청 승인
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void approve(String moimCode, String leaderMemberCode, String applicantMemberCode) {
        Moim moim = moimAccessSupportService.getAccessibleMoim(moimCode, leaderMemberCode);

        validateLeader(moim, leaderMemberCode);
        validateMoimDate(moim);
        validateBlockRelation(leaderMemberCode, applicantMemberCode);
        validateCapacity(moim);

        MoimMemberMapping mapping = getApplyMapping(moimCode, applicantMemberCode);
        validatePending(mapping);

        mapping.approve();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 모임 신청 거절
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void reject(String moimCode, String leaderMemberCode, String applicantMemberCode) {
        Moim moim = moimAccessSupportService.getAccessibleMoim(moimCode, leaderMemberCode);

        validateLeader(moim, leaderMemberCode);
        validateMoimDate(moim);

        MoimMemberMapping mapping = getApplyMapping(moimCode, applicantMemberCode);
        validatePending(mapping);

        mapping.reject();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 내부 검증
    // ─────────────────────────────────────────────────────────────────────────

    private void validateNotLeader(Moim moim, String memberCode) {
        if (moim.getLeaderMemberCode().equals(memberCode)) {
            throw new BusinessException(MoimErrorCode.MOIM_APPLY_FORBIDDEN_TO_LEADER);
        }
    }

    private void validateLeader(Moim moim, String leaderMemberCode) {
        if (!moim.getLeaderMemberCode().equals(leaderMemberCode)) {
            throw new BusinessException(MoimErrorCode.MOIM_APPROVE_REJECT_FORBIDDEN);
        }
    }

    private void validateMoimDate(Moim moim) {
        if (moim.getDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException(MoimErrorCode.MOIM_APPLY_CLOSED);
        }
    }

    private void validateDuplicateApply(String moimCode, String memberCode) {
        boolean alreadyApplied = moimMemberMappingRepository
                .existsByIdMoimCodeAndIdMemberCodeAndStatusIn(
                        moimCode,
                        memberCode,
                        List.of(MoimMemberStatus.PENDING, MoimMemberStatus.APPROVED)
                );

        if (alreadyApplied) {
            throw new BusinessException(MoimErrorCode.MOIM_ALREADY_APPLIED);
        }
    }

    private void validateCapacity(Moim moim) {
        long approvedCount = moimMemberMappingRepository
                .countByIdMoimCodeAndStatus(moim.getMoimCode(), MoimMemberStatus.APPROVED);

        if (approvedCount >= moim.getMaxParticipants()) {
            throw new BusinessException(MoimErrorCode.MOIM_FULL);
        }
    }

    private void validateBlockRelation(String leaderMemberCode, String applicantMemberCode) {
        boolean leaderBlockedApplicant = memberBlockMappingRepository
                .existsByIdBlockerCodeAndIdBlockedCodeAndDeleteYn(
                        leaderMemberCode, applicantMemberCode, YesNo.NO
                );

        boolean applicantBlockedLeader = memberBlockMappingRepository
                .existsByIdBlockerCodeAndIdBlockedCodeAndDeleteYn(
                        applicantMemberCode, leaderMemberCode, YesNo.NO
                );

        if (leaderBlockedApplicant || applicantBlockedLeader) {
            throw new BusinessException(MoimErrorCode.MOIM_MEMBER_BLOCKED);
        }
    }

    private MoimMemberMapping getApplyMapping(String moimCode, String applicantMemberCode) {
        return moimMemberMappingRepository
                .findByIdMoimCodeAndIdMemberCode(moimCode, applicantMemberCode)
                .orElseThrow(() -> new BusinessException(MoimErrorCode.MOIM_APPLY_NOT_FOUND));
    }

    private void validatePending(MoimMemberMapping mapping) {
        if (!mapping.isPending()) {
            throw new BusinessException(MoimErrorCode.MOIM_APPLY_ALREADY_PROCESSED);
        }
    }
}
