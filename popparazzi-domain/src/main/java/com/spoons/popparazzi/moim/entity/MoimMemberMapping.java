package com.spoons.popparazzi.moim.entity;

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

    @Column(name = "mp_join_yn", length = 1, nullable = false, columnDefinition = "char(1)")
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
