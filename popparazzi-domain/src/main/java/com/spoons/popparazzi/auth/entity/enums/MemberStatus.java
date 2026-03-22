package com.spoons.popparazzi.auth.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberStatus {

    ACTIVE("활성"),
    INACTIVE("비활성"),
    BANNED("정지"),
    WITHDRAWN("탈퇴");

    private final String description;
}
