package com.spoons.popparazzi.auth.service;

import kr.co.hs.domain.modules.member.domain.constant.ProviderType;
import kr.co.hs.domain.modules.member.domain.model.Member;
import kr.co.hs.domain.modules.member.infrastructure.platform.MemberJpaRepository;
import kr.co.hs.domain.modules.member.infrastructure.platform.MemberQueryRepository;
import kr.co.hs.util.exception.MarkException;
import kr.co.hs.util.exception.type.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static kr.co.hs.domain.modules.member.domain.constant.Status.ACTIVITY;
import static kr.co.hs.domain.modules.member.domain.constant.ProviderType.LOCAL;
import static kr.co.hs.util.exception.type.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class LoginService implements UserDetailsService {

    public final MemberJpaRepository memberJpaRepository;
    public final MemberQueryRepository memberQueryRepository;


    @Override
    @Transactional
    public UserDetails loadUserByUsername(String memberId) throws UsernameNotFoundException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getDetails() == null) {
            throw new UsernameNotFoundException("Authentication details not found");
        }

        ProviderType provider = (ProviderType) SecurityContextHolder.getContext().getAuthentication().getDetails();  // details에서 provider 추출

        Member member = memberJpaRepository.findByMemberIdAndProvider(memberId, provider)
                .orElseThrow( () -> new MarkException(USER_NOT_FOUND));

        if (member != null) {

            if (provider.equals(LOCAL)) {
                return createUserDetails(member);
            } else {
                return createUserDetailsForSocial(member);
            }
        } else {
            throw new MarkException(ErrorCode.USER_NOT_FOUND);
        }
    }

    // DB에 Member 값이 존재한다면 UserDetails 객체로 만들어서 리턴
    private UserDetails createUserDetails(Member member) {

        return User.builder()
                .username(member.getMemberCode())
                .password(member.getPassword())
                .roles(member.getAuthority().name())
                // 계정 비활성화시 Exception 처리
                .disabled( !member.getStatus().equals(ACTIVITY))
                .build();

    }

    private UserDetails createUserDetailsForSocial(Member member) {

        return User.builder()
                .username(member.getMemberCode())
                .password("SOCIAL_USER")
                .roles(member.getAuthority().name())
                // 계정 비활성화시 Exception 처리
                .disabled( !member.getStatus().equals(ACTIVITY))
                .build();

    }

}
