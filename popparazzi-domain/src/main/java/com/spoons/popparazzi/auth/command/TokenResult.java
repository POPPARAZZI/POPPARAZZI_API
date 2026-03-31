package com.spoons.popparazzi.auth.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TokenResult {
    private final String accessToken;
    private final String refreshToken;

    public static TokenResult of(String accessToken, String refreshToken) {
        return new TokenResult(accessToken, refreshToken);
    }
}
