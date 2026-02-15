package com.spoons.popparazzi.like.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "tbl_like_mapping",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_tbl_like_mapping",
                        columnNames = {"lm_tmm_code", "lm_pmm_code", "lm_type"}
                )
        }
)
@NoArgsConstructor
public class LikeMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lm_seq")
    private Long id;

    @Column(name = "lm_tmm_code", nullable = false, length = 22)
    private String memberCode;

    @Column(name = "lm_pmm_code", nullable = false, length = 22)
    private String targetCode;

    @Convert(converter = LikeTypeConverter.class)
    @Column(name = "lm_type", nullable = false, length = 1)
    private LikeType type;

    @Column(name = "lm_reg_dt", nullable = false)
    private LocalDateTime createdAt;

    public LikeMapping(String memberCode, String targetCode, LikeType type) {
        this.memberCode = memberCode;
        this.targetCode = targetCode;
        this.type = type;
        this.createdAt = LocalDateTime.now();
    }
}
