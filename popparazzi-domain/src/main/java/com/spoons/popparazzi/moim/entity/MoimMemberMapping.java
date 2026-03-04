package com.spoons.popparazzi.moim.entity;

import com.spoons.popparazzi.common.YesNo;
import jakarta.persistence.*;
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

    // ✅ 신청자 기본 생성: 승인 대기 + join YES(참가 의사 있음)
    public static MoimMemberMapping applicant(String mmCode, String tmmCode) {
        return new MoimMemberMapping(mmCode, tmmCode, false, YesNo.YES);
    }

    // ✅ 방장 생성: 자동 승인 + join YES
    public static MoimMemberMapping leader(String mmCode, String tmmCode) {
        return new MoimMemberMapping(mmCode, tmmCode, true, YesNo.YES);
    }

    // 내부 전용 생성자 (의도는 팩토리에서만 결정)
    private MoimMemberMapping(String mmCode, String tmmCode, boolean approved, YesNo joinYn) {
        this.id = new MoimMemberMappingId(mmCode, tmmCode);
        this.isApproved = approved;
        this.joinYn = joinYn;
    }

    // 필요하면 “승인” 행위도 도메인 메서드로
    public void approve() {
        this.isApproved = true;
    }

    public boolean isJoined() {
        return this.joinYn != null && this.joinYn.isYes();
    }
}