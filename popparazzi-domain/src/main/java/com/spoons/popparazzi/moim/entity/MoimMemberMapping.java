package com.spoons.popparazzi.moim.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "TBL_MOIM_MEMBER_MAPPING")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MoimMemberMapping {

    @EmbeddedId
    private MoimMemberMappingId id;

    @Column(name = "MP_IS_APPROVED", nullable = false)
    private boolean isApproved;

    @Column(name = "MP_JOIN_YN", nullable = false, length = 1)
    private String joinYn;

    public MoimMemberMapping(String mmCode, String tmmCode) {
        this.id = new MoimMemberMappingId(mmCode, tmmCode);
        this.isApproved = false;
        this.joinYn = "Y";
    }

    public boolean isJoined() {
        return "Y".equalsIgnoreCase(this.joinYn);
    }
}
