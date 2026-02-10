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

        @Column(name = "MP_MM_CODE", length = 22, nullable = false)
        private String mmCode;

        @Column(name = "MP_TMM_CODE", length = 22, nullable = false)
        private String tmmCode;
}
