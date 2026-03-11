package com.spoons.popparazzi.member.entity;

import com.spoons.popparazzi.common.YesNo;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_member_block_mapping")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberBlockMapping {

    @EmbeddedId
    private MemberBlockMappingId id;

    @Column(name = "mbm_reg_dt", nullable = false)
    private LocalDateTime regDt;

    @Column(name = "mbm_delete_yn", length = 1, nullable = false)
    private YesNo deleteYn;

    @Column(name = "mbm_delete_dt")
    private LocalDateTime deleteDt;

    public static MemberBlockMapping create(String blockerCode, String blockedCode) {
        MemberBlockMapping mapping = new MemberBlockMapping();
        mapping.id = new MemberBlockMappingId(blockerCode, blockedCode);
        mapping.regDt = LocalDateTime.now();
        mapping.deleteYn = YesNo.NO;
        mapping.deleteDt = null;
        return mapping;
    }

    public void unblock() {
        this.deleteYn = YesNo.YES;
        this.deleteDt = LocalDateTime.now();
    }

    public void reblock() {
        this.deleteYn = YesNo.NO;
        this.deleteDt = null;
    }

    public boolean isActiveBlock() {
        return this.deleteYn != null && this.deleteYn.isNo();
    }
}