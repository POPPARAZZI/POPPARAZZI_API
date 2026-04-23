package com.spoons.popparazzi.moim.entity;

import com.spoons.popparazzi.common.YesNo;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_moim_member_mapping")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MoimMemberMapping {

    @EmbeddedId
    private MoimMemberMappingId id;

    @Column(name = "mp_is_approved", nullable = false)
    private boolean isApproved;

    @Column(name = "mp_join_yn", length = 1, nullable = false)
    private YesNo joinYn;

    @Column(name = "mp_answer")
    private String answer;

    // 신청자 생성 (모임 신청)
    public static MoimMemberMapping applicant(String moimCode, String memberCode, String answer) {
        return new MoimMemberMapping(moimCode, memberCode, false, YesNo.YES, answer);
    }

    // 모임장 생성 (모임 생성 시 자동 등록)
    public static MoimMemberMapping leader(String moimCode, String memberCode) {
        return new MoimMemberMapping(moimCode, memberCode, true, YesNo.YES, null);
    }

    private MoimMemberMapping(String moimCode, String memberCode, boolean approved, YesNo joinYn, String answer) {
        this.id = new MoimMemberMappingId(moimCode, memberCode);
        this.isApproved = approved;
        this.joinYn = joinYn;
        this.answer = answer;
    }

    // 모임 승인
    public void approve() {
        this.isApproved = true;
        this.joinYn = YesNo.YES;
    }

    // 모임 거절
    public void reject() {
        this.isApproved = false;
        this.joinYn = YesNo.NO;
    }

    // 대기 상태인지 확인
    public boolean isPending() {
        return !this.isApproved && this.joinYn != null && this.joinYn.isYes();
    }

    // 승인 완료 상태인지 확인
    public boolean isApprovedMember() {
        return this.isApproved && this.joinYn != null && this.joinYn.isYes();
    }

    // 거절 상태인지 확인
    public boolean isRejected() {
        return !this.isApproved && this.joinYn != null && this.joinYn.isNo();
    }

    public boolean isJoined() {
        return this.joinYn != null && this.joinYn.isYes();
    }
}