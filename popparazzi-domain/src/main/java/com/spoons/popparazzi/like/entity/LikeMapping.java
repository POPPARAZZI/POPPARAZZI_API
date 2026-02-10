package com.spoons.popparazzi.like.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "TBL_LIKE_MAPPING",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_TBL_LIKE_MAPPING",
                        columnNames = {"LM_TMM_CODE", "LM_PMM_CODE", "LM_TYPE"}
                )
        }
)
@NoArgsConstructor
public class LikeMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LM_SEQ")
    private Long id;

    @Column(name = "LM_TMM_CODE", nullable = false, length = 22)
    private String memberCode;

    @Column(name = "LM_PMM_CODE", nullable = false, length = 22)
    private String targetCode;

    @Column(name = "LM_TYPE", nullable = false, length = 2)
    private LikeType type;

    @Column(name = "LM_REG_DT", nullable = false)
    private LocalDateTime createdAt;

    public LikeMapping(String memberCode, String targetCode, LikeType type) {
        this.memberCode = memberCode;
        this.targetCode = targetCode;
        this.type = type;
        this.createdAt = LocalDateTime.now();
    }
}
