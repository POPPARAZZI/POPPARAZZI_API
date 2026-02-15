package com.spoons.popparazzi.moim.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/*
 * DB랑 연결하는 엔티티
 */
@Entity
@Table(name = "tbl_moim_master")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
public class Moim {

    @Id
    @Column(name = "mm_code", length = 22, nullable = false)
    private String moimCode;

    @Column(name = "mm_pm_code", length = 22, nullable = false)
    private String popupCode;

    @Column(name = "mm_tmm_code", length = 22, nullable = false)
    private String leaderMemberCode;

    @Column(name = "mm_date", nullable = false)
    private LocalDateTime date;

    @Column(name = "mm_max_participants", nullable = false)
    private Integer maxParticipants;

    @Column(name = "mm_title", length = 100, nullable = false)
    private String title;

    @Column(name = "mm_body", nullable = false, columnDefinition = "text")
    private String body;

    @Column(name = "mm_pre_question", nullable = false, columnDefinition = "text")
    private String preQuestion;

    @Column(name = "mm_reg_dt", nullable = false)
    private LocalDateTime regDt;

    @Column(name = "mm_delete_yn", length = 1, nullable = false, columnDefinition = "char(1)")
    private String deleteYn;

    @Column(name = "mm_delete_dt")
    private LocalDateTime deleteDt;
}
