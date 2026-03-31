package com.spoons.popparazzi.auth.service;

import com.spoons.popparazzi.auth.entity.Member;
import com.spoons.popparazzi.auth.entity.enums.SnsType;
import com.spoons.popparazzi.auth.repository.AuthJpaRepository;
import com.spoons.popparazzi.auth.service.social.GoogleAuthService;
import com.spoons.popparazzi.auth.service.social.KakaoAuthService;
import com.spoons.popparazzi.auth.service.social.NaverAuthService;
import com.spoons.popparazzi.auth.service.social.SocialAuthService;
import com.spoons.popparazzi.jwt.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SocialAuthServiceFactory {

    private final AuthJpaRepository authJpaRepository;
    private final JwtService jwtService;

    private final KakaoAuthService kakaoAuthService;
    private final NaverAuthService naverAuthService;
    private final GoogleAuthService googleAuthService;

    public SocialAuthService getService(SnsType snsType) {
        return switch (snsType) {
            case K -> kakaoAuthService;
            case N -> naverAuthService;
            case G -> googleAuthService;

            default -> throw new IllegalArgumentException("Unknown provider: " + snsType);
        };
    }

    public Member findByMemberIdAndProvider(String memberId, SnsType snsType) {
        return authJpaRepository.findByMemberIdAndSnsType(memberId, snsType).orElse(null);
    }

/*    @Transactional
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

        authJpaRepository.save(member);

    }*/

}
