package com.spoons.popparazzi.moim.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class MoimMemberMappingId implements Serializable {

        @Column(name = "mp_mm_code", length = 22, nullable = false)
        private String moimCode;

        @Column(name = "mp_tmm_code", length = 22, nullable = false)
        private String memberCode;
}
