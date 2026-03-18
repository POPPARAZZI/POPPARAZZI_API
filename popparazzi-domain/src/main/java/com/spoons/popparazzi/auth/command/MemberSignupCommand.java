package com.spoons.popparazzi.auth.command;

public record MemberSignupCommand(String email, String pwd, String nickName, String gender) {

}
