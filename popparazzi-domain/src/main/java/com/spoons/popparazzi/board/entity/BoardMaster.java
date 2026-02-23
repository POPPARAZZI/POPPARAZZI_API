package com.spoons.popparazzi.board.entity;

import com.spoons.popparazzi.board.enums.BoardType;
import com.spoons.popparazzi.common.YesNo;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "tbl_board_master")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardMaster {

    @Id
    @Column(name = "tbm_code", length = 22, nullable = false)
    private String boardCode;

    @Column(name = "tbm_tmm_code", length = 22, nullable = false)
    private String memberCode;

    @Column(name = "tbm_title", length = 100)
    private String title;

    @Column(name = "tbm_body", length = 255, nullable = false)
    private String body;

    @Column(name = "tbm_photo_yn", length = 1, nullable = false)
    private YesNo photoYn;

    @Column(name = "tbm_rating")
    private Integer rating;

    @Enumerated(EnumType.STRING)
    @Column(name = "tbm_type", length = 1, nullable = false)
    private BoardType type;

    @Column(name = "tbm_reg_dt", nullable = false)
    private LocalDateTime regDt;

    @Column(name = "tbm_delete_yn", length = 1, nullable = false)
    private YesNo deleteYn;

    @Column(name = "tbm_delete_dt")
    private LocalDateTime deleteDt;

    @Builder
    private BoardMaster(
            String boardCode,
            String memberCode,
            String title,
            String body,
            YesNo photoYn,
            Integer rating,
            BoardType type,
            LocalDateTime regDt,
            YesNo deleteYn,
            LocalDateTime deleteDt
    ) {
        this.boardCode = boardCode;
        this.memberCode = memberCode;
        this.title = title;
        this.body = body;
        this.photoYn = photoYn;
        this.rating = rating;
        this.type = type;
        this.regDt = regDt;
        this.deleteYn = deleteYn;
        this.deleteDt = deleteDt;
    }
}