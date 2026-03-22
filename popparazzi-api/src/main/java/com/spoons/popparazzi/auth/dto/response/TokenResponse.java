package com.spoons.popparazzi.auth.dto.response;

import com.spoons.popparazzi.auth.command.TokenResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TokenResponse {

    private final String accessToken;
    private final String refreshToken;

    public static TokenResponse from(TokenResult result) {
        return new TokenResponse(
                result.getAccessToken(),
                result.getRefreshToken()
        );
    }
}
