package com.spoons.popparazzi.moim.service;

import com.spoons.popparazzi.common.YesNo;
import com.spoons.popparazzi.error.exception.BusinessException;
import com.spoons.popparazzi.moim.dto.command.ApplyMoimCommand;
import com.spoons.popparazzi.moim.dto.query.MoimApplyInfoQuery;
import com.spoons.popparazzi.moim.dto.query.MoimParticipantQuery;
import com.spoons.popparazzi.moim.dto.result.MoimApplyInfoResult;
import com.spoons.popparazzi.moim.dto.result.MoimParticipantsResult;
import com.spoons.popparazzi.moim.entity.Moim;
import com.spoons.popparazzi.moim.entity.MoimMemberMapping;
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

    // 1. 모임 참여 화면 조회
    @Override
    @Transactional(readOnly = true)
    public MoimApplyInfoResult getApplyInfo(String moimCode, String memberCode) {
        moimAccessSupportService.getAccessibleMoim(moimCode, memberCode);

        MoimApplyInfoQuery query = moimQueryRepository.findApplyInfoByMoimCode(moimCode)
                .orElseThrow(() -> new BusinessException(MoimErrorCode.MOIM_NOT_FOUND));

        return new MoimApplyInfoResult(
                query.leaderProfileImageUrl(),
                query.leaderNickname(),
                query.question()
        );
    }

    // 2. 모임 참여 신청
    @Override
    public void apply(String moimCode, String memberCode, ApplyMoimCommand command) {
        Moim moim = moimAccessSupportService.getAccessibleMoim(moimCode, memberCode);

        validateNotLeader(moim, memberCode);
        validateMoimDate(moim);
        validateDuplicateApply(moimCode, memberCode);
        validateCapacity(moim);

        MoimMemberMapping mapping =
                MoimMemberMapping.applicant(moimCode, memberCode, command.answer());

        moimMemberMappingRepository.save(mapping);
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

    // 3. 모임 참여 멤버 조회
    @Override
    @Transactional(readOnly = true)
    public MoimParticipantsResult getParticipants(String moimCode, String memberCode) {
        moimAccessSupportService.getAccessibleMoim(moimCode, memberCode);

        List<MoimParticipantQuery> participants =
                moimQueryRepository.findParticipantsByMoimCode(moimCode);

        return MoimParticipantsResult.from(participants);
    }
}