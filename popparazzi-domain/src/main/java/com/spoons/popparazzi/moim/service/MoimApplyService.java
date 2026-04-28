package com.spoons.popparazzi.moim.service;

import com.spoons.popparazzi.moim.dto.command.ApplyMoimCommand;
import com.spoons.popparazzi.moim.dto.result.MoimApplyInfoResult;
import com.spoons.popparazzi.moim.dto.result.MoimParticipantsResult;

public interface MoimApplyService {

    /**
     * @methodName  : getApplyInfo
     * @author      : seulgi Yang
     * @param       : moimCode, memberCode
     * @returnType  : MoimApplyInfoResult
     * @desc        : 모임 신청 화면 조회 (방장 프로필 + 사전 질문)
     */
    MoimApplyInfoResult getApplyInfo(String moimCode, String memberCode);

    /**
     * @methodName  : apply
     * @author      : seulgi Yang
     * @param       : moimCode, memberCode, ApplyMoimCommand
     * @returnType  : void
     * @desc        : 모임 참여 신청. 방장 신청 불가, 중복 신청 불가, 정원 초과 불가
     */
    void apply(String moimCode, String memberCode, ApplyMoimCommand command);

    /**
     * @methodName  : getParticipants
     * @author      : seulgi Yang
     * @param       : moimCode, memberCode
     * @returnType  : MoimParticipantsResult
     * @desc        : 모임 참여자 목록 조회 (방장 우선 정렬)
     */
    MoimParticipantsResult getParticipants(String moimCode, String memberCode);

    /**
     * @methodName  : approve
     * @author      : seulgi Yang
     * @param       : moimCode, leaderMemberCode, applicantMemberCode
     * @returnType  : void
     * @desc        : 모임 신청 승인. 모임장만 승인 가능하며, 대기 상태 신청 건만 승인 처리
     */
    void approve(String moimCode, String leaderMemberCode, String applicantMemberCode);

    /**
     * @methodName  : reject
     * @author      : seulgi Yang
     * @param       : moimCode, leaderMemberCode, applicantMemberCode
     * @returnType  : void
     * @desc        : 모임 신청 거절. 모임장만 거절 가능하며, 대기 상태 신청 건만 거절 처리
     */
    void reject(String moimCode, String leaderMemberCode, String applicantMemberCode);
}