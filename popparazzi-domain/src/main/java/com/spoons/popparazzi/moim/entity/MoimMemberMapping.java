package com.spoons.popparazzi.moim.entity;

import com.spoons.popparazzi.moim.enums.MoimMemberStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_moim_member_mapping")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MoimMemberMapping {

    @EmbeddedId
    private MoimMemberMappingId id;

    @Enumerated(EnumType.STRING)
    @Column(name = "mp_status", length = 20, nullable = false)
    private MoimMemberStatus status;

    @Column(name = "mp_answer")
    private String answer;

    @Column(name = "mp_reg_dt", nullable = false)
    private LocalDateTime regDt;

    @Column(name = "mp_processed_dt")
    private LocalDateTime processedDt;

    // 신청자 생성 (모임 신청)
    public static MoimMemberMapping applicant(String moimCode, String memberCode, String answer) {
        return new MoimMemberMapping(
                moimCode,
                memberCode,
                MoimMemberStatus.PENDING,
                answer,
                LocalDateTime.now(),
                null
        );
    }

    // 모임장 생성 (모임 생성 시 자동 등록)
    public static MoimMemberMapping leader(String moimCode, String memberCode) {
        return new MoimMemberMapping(
                moimCode,
                memberCode,
                MoimMemberStatus.APPROVED,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private MoimMemberMapping(
            String moimCode,
            String memberCode,
            MoimMemberStatus status,
            String answer,
            LocalDateTime regDt,
            LocalDateTime processedDt
    ) {
        this.id = new MoimMemberMappingId(moimCode, memberCode);
        this.status = status;
        this.answer = answer;
        this.regDt = regDt;
        this.processedDt = processedDt;
    }

    // 모임 승인
    public void approve() {
        this.status = MoimMemberStatus.APPROVED;
        this.processedDt = LocalDateTime.now();
    }

    // 모임 거절
    public void reject() {
        this.status = MoimMemberStatus.REJECTED;
        this.processedDt = LocalDateTime.now();
    }

    // 대기 상태인지 확인
    public boolean isPending() {
        return this.status == MoimMemberStatus.PENDING;
    }

    // 승인 완료 상태인지 확인
    public boolean isApprovedMember() {
        return this.status == MoimMemberStatus.APPROVED;
    }

    // 거절 상태인지 확인
    public boolean isRejected() {
        return this.status == MoimMemberStatus.REJECTED;
    }

    // 현재 참여 중인지 확인
    public boolean isJoined() {
        return this.status == MoimMemberStatus.APPROVED;
    }
}