package com.spoons.popparazzi.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MemberBlockMappingId implements Serializable {

    @Column(name = "mbm_blocker_code", length = 22, nullable = false)
    private String blockerCode;

    @Column(name = "mbm_blocked_code", length = 22, nullable = false)
    private String blockedCode;
}