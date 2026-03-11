package com.spoons.popparazzi.member.service.support;

public interface MemberBlockSupportService {

    boolean isBlockedBetween(String memberCode, String targetMemberCode);
}