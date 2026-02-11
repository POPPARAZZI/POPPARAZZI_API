package com.spoons.popparazzi.jwt;

import lombok.Getter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class CustomUser extends User {

    private final String uCode;

    private final String spCode;

    private final int level;

    private final String adultHiddenYn;

    public CustomUser (String uCode, String spCode, UserDetails userDetails, int level, String adultHiddenYn) {

        super(
                userDetails.getUsername(),
                userDetails.getPassword(),
                userDetails.getAuthorities()
        );

        this.uCode = uCode;
        this.spCode = spCode;
        this.level = level;
        this.adultHiddenYn = adultHiddenYn;
    }

    public static CustomUser of(String uCode, String spCode, UserDetails userDetails, int level, String adultHiddenYn) {

        return new CustomUser(
                uCode,
                spCode,
                userDetails,
                level,
                adultHiddenYn
        );
    }
}
