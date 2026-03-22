package com.spoons.popparazzi.auth.service;

import com.spoons.popparazzi.auth.command.MemberLoginCommand;
import com.spoons.popparazzi.auth.command.MemberSignupCommand;
import com.spoons.popparazzi.auth.command.TokenResult;

public interface AuthService {

    /**
     *
     * @methodName	: signup
     * @author		: inseon kang
     * @param       : MemberSignupCommand
     * @date		:2026.03.18
     * @returnType	: void
     * @desc		: 회원가입
     */
    void signup(MemberSignupCommand memberCommand);
    /**
     *
     * @methodName	: login
     * @author		: inseon kang
     * @param       : MemberLoginCommand
     * @date		:2026.03.22
     * @returnType	: TokenResponse
     * @desc		: 로그인
     */
    TokenResult login(MemberLoginCommand memberCommand);
}
