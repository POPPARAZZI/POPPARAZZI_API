package com.spoons.popparazzi.moim.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/*
* DB랑 연결하는 엔티티
* */
@Entity
@Table(name = "TBL_MOIM_MASTER")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
public class Moim {

    @Id
    @Column(name = "MM_CODE", length = 22, nullable = false)
    private String mmCode;

    @Column(name = "MM_PM_CODE", length = 22, nullable = false)
    private String mmPmCode;

    @Column(name = "MM_TITLE", length = 100, nullable = false)
    private String mmTitle;

    @Column(name = "MM_DATE", nullable = false)
    private LocalDateTime mmDate;

    @Column(name = "MM_MAX_PARTICIPANTS", nullable = false)
    private Integer mmMaxParticipants;

    @Column(name = "MM_REG_DT", nullable = false)
    private LocalDateTime mmRegDt;

    @Column(name = "MM_DELETE_YN", length = 1, nullable = false)
    private String mmDeleteYn;

}
