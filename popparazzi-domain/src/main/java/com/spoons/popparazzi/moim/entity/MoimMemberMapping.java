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

    public MoimMemberMapping(String mmCode, String tmmCode) {
        this.id = new MoimMemberMappingId(mmCode, tmmCode);
        this.isApproved = false;
        this.joinYn = YesNo.YES;
    }

    public boolean isJoined() {
        return this.joinYn != null && this.joinYn.isYes();
    }
}
