package com.spoons.popparazzi.file.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_file_master")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fm_seq")
    private Long fmSeq;

    @Column(name = "fm_parent_code", length = 22, nullable = false)
    private String parentCode;

    @Column(name = "fm_url", length = 255, nullable = false)
    private String url;

    @Convert(converter = FileTypeConverter.class)
    @Column(name="fm_type", nullable=false, length=1)
    private FileType fmType;

    @Column(name = "fm_reg_dt", nullable = false)
    private LocalDateTime regDt;
}
