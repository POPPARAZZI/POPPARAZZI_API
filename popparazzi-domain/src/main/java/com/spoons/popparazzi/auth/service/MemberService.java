package com.spoons.popparazzi.auth.service;

import com.spoons.popparazzi.auth.command.MemberSignupCommand;

public interface MemberService {

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
}
