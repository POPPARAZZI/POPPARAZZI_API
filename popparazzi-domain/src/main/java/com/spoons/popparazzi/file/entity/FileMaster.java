package com.spoons.popparazzi.file.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "TBL_FILE_MASTER")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FM_SEQ")
    private Long fmSeq;

    @Column(name = "FM_PARENT_CODE", length = 22, nullable = false)
    private String parentCode;

    @Column(name = "FM_URL", length = 255, nullable = false)
    private String url;

    @Column(name = "FM_TYPE", nullable = false)
    private FileType type;

    @Column(name = "FM_REG_DT", nullable = false)
    private LocalDateTime regDt;
}
