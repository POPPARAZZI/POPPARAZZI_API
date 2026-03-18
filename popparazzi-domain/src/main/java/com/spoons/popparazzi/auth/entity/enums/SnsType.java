package com.spoons.popparazzi.auth.entity.enums;

import lombok.Getter;

@Getter
public enum SnsType {
    G("Google"),
    N("Naver"),
    K("Kakao"),
    E("Email");

    private final String description;

    SnsType(String description) {
        this.description = description;
    }
}
