package com.spoons.popparazzi.auth.service;

import com.spoons.popparazzi.auth.command.MemberLoginCommand;
import com.spoons.popparazzi.auth.command.MemberSignupCommand;
import com.spoons.popparazzi.auth.command.TokenResult;
import com.spoons.popparazzi.auth.entity.enums.MemberStatus;
import com.spoons.popparazzi.auth.entity.enums.SnsType;
import com.spoons.popparazzi.auth.repository.AuthJpaRepository;
import com.spoons.popparazzi.error.exception.BusinessException;
import com.spoons.popparazzi.jwt.service.CustomUserDetailsService;
import com.spoons.popparazzi.jwt.service.JwtService;
import com.spoons.popparazzi.member.entity.Member;
import com.spoons.popparazzi.seq.service.SeqService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.spoons.popparazzi.auth.error.AuthErrorCode.ALREADY_SIGNUP;
import static com.spoons.popparazzi.auth.error.AuthErrorCode.INVALID_MEMBER;
import static com.spoons.popparazzi.error.code.CommonErrorCode.MEMBER_NOT_ACTIVITY;
import static com.spoons.popparazzi.error.code.CommonErrorCode.MEMBER_NOT_FOUND;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

    private final AuthJpaRepository authJpaRepository;
    private final SeqService seqService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    @Transactional
    public void signup(MemberSignupCommand memberCommand) {

        if(authJpaRepository.existsByEmail(memberCommand.email())) {
            throw new BusinessException(ALREADY_SIGNUP);
        }

        Member member = new Member(memberCommand.email()
                , passwordEncoder.encode(memberCommand.pwd())
                , memberCommand.email()
                , memberCommand.nickName()
                , memberCommand.gender()
                , SnsType.E
                , seqService.getUuid());

        seqService.getSeqCode(member);

        System.out.println("before save: " + member.getMemberCode());

        authJpaRepository.save(member);

    }

    @Override
    public TokenResult login(MemberLoginCommand memberCommand) {

        // 1. 이메일로 유저 조회
        Member member = authJpaRepository.findByMemberId(memberCommand.id())
                .orElseThrow(() -> new BusinessException(MEMBER_NOT_FOUND));

        // 2. 계정 상태 체크
        if (!member.getStatus().equals(MemberStatus.ACTIVE)) {
            throw new BusinessException(MEMBER_NOT_ACTIVITY);
        }

        // 3. 비밀번호 검증
        if (!passwordEncoder.matches(memberCommand.pwd(), member.getMemberPwd())) {
            throw new BusinessException(INVALID_MEMBER);
        }

        // 5. 토큰 생성
        String accessToken = jwtService.createAccessToken(member);
        String refreshToken = jwtService.createRefreshToken();

        // 6. RefreshToken 저장
        member.updateToken(refreshToken);

        return TokenResult.of(accessToken, refreshToken);
    }

}
