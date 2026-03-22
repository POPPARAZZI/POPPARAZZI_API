package com.spoons.popparazzi.jwt.service;

import com.spoons.popparazzi.auth.entity.Member;
import com.spoons.popparazzi.auth.entity.enums.MemberStatus;
import com.spoons.popparazzi.auth.entity.enums.SnsType;
import com.spoons.popparazzi.auth.repository.AuthJpaRepository;
import com.spoons.popparazzi.error.exception.BusinessException;
import com.spoons.popparazzi.jwt.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.spoons.popparazzi.auth.entity.enums.SnsType.E;
import static com.spoons.popparazzi.error.code.CommonErrorCode.MEMBER_NOT_ACTIVITY;
import static com.spoons.popparazzi.error.code.CommonErrorCode.MEMBER_NOT_FOUND;


@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    public final AuthJpaRepository authJpaRepository;

    // 로그인 시 — 이메일로 조회
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String memberId) throws UsernameNotFoundException {

        Member member = authJpaRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(MEMBER_NOT_FOUND));

        if (!member.getStatus().equals(MemberStatus.ACTIVE)) {
            throw new BusinessException(MEMBER_NOT_ACTIVITY);
        }

        return new CustomUserDetails(member);
    }

    // 토큰 검증 시 — UUID로 조회
    @Transactional(readOnly = true)
    public UserDetails loadUserByMemberUuid(String memberUuid) {
        Member member = authJpaRepository.findByMemberUuid(memberUuid)
                .orElseThrow(() -> new BusinessException(MEMBER_NOT_FOUND));

        if (!member.getStatus().equals(MemberStatus.ACTIVE)) {
            throw new BusinessException(MEMBER_NOT_ACTIVITY);
        }

        return new CustomUserDetails(member);
    }

}
