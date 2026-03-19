package com.spoons.popparazzi.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class SocialAuthServiceFactory {

    private final MemberJpaRepository memberJpaRepository;
    private final JwtService jwtService;

    private final KakaoAuthService kakaoAuthService;
    private final NaverAuthService naverAuthService;
    private final GoogleAuthService googleAuthService;
    private final AppleAuthService appleAuthService;

    public SocialAuthService getService(ProviderType provider) {
        return switch (provider) {
            case KAKAO -> kakaoAuthService;
            case NAVER -> naverAuthService;
            case GOOGLE -> googleAuthService;
            case APPLE -> appleAuthService;

            default -> throw new IllegalArgumentException("Unknown provider: " + provider);
        };
    }

    public Member findByMemberIdAndProvider(String email, ProviderType provider) {
        return memberJpaRepository.findByMemberIdAndProvider(email, provider).orElse(null);
    }

    @Transactional
    public void signup(SocialUserInfo userInfo) {
        System.out.println("아이디  = " + userInfo.getId());
        System.out.println("어디  = " + userInfo.getProvider());

        Member member = Member.builder()
                .memberId(userInfo.getEmail())
                .email(userInfo.getEmail())
                .name(userInfo.getName())
                .authority(RoleType.MEMBER)
                .status(Status.ACTIVITY)
                .provider((userInfo.getProvider()))
                .build();

        memberJpaRepository.save(member);

    }

    public Member existsByMember(String email) {

        return memberJpaRepository.findByEmail(email).orElse(null);
    }

}
