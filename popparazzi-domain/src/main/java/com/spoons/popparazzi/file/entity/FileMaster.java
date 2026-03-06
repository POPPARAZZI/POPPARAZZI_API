package com.spoons.popparazzi.file.entity;

import com.spoons.popparazzi.common.YesNo;
import com.spoons.popparazzi.file.enums.FileType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

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

    @Enumerated(EnumType.STRING)
    @Column(name="fm_type", nullable=false, length=1)
    private FileType fmType;

    @Column(name = "fm_delete_yn", length = 1, nullable = false)
    private YesNo deleteYn = YesNo.NO;

    @CreationTimestamp
    @Column(name = "fm_reg_dt", nullable = false)
    private LocalDateTime regDt;

    // ✅ 생성용 팩토리
    public static FileMaster create(String parentCode, String url, FileType fileType) {
        FileMaster f = new FileMaster();
        f.parentCode = parentCode;
        f.url = url;
        f.fmType = fileType;
        f.regDt = LocalDateTime.now();
        f.deleteYn = YesNo.NO;
        return f;
    }

    // 소프트 삭제
    public void softDelete() {
        this.deleteYn = YesNo.YES;
    }

    public boolean isDeleted() {
        return this.deleteYn != null && this.deleteYn.isYes();
    }
}
