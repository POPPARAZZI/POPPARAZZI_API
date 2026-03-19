package com.spoons.popparazzi.auth.service;

import jakarta.servlet.http.HttpServletResponse;
import kr.co.hs.domain.modules.member.domain.constant.ProviderType;
import kr.co.hs.domain.modules.member.domain.constant.RoleType;
import kr.co.hs.domain.modules.member.domain.model.Member;
import kr.co.hs.domain.modules.member.infrastructure.dto.request.MemberRequestDTO;
import kr.co.hs.domain.modules.member.infrastructure.platform.MemberJpaRepository;
import kr.co.hs.domain.modules.point.domain.model.Point;
import kr.co.hs.domain.modules.point.infrastructure.platform.PointJpaRepository;
import kr.co.hs.domain.modules.termsOfUse.domain.TermsOfUse;
import kr.co.hs.domain.modules.termsOfUse.infrastructure.platform.TermsOfUseJpaRepository;
import kr.co.hs.util.enc.EncUtils;
import kr.co.hs.util.exception.MarkException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import static kr.co.hs.domain.modules.member.domain.constant.Status.ACTIVITY;
import static kr.co.hs.util.exception.type.ErrorCode.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberJpaRepository memberJpaRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    private final TermsOfUseJpaRepository termsOfUseJpaRepository;
    private final PointJpaRepository pointJpaRepository;

    @Transactional
    public void signup(MemberRequestDTO memberRequest) throws Exception {

        if(memberJpaRepository.existsByMemberId(memberRequest.memberId())) {
            throw new MarkException(ALREADY_SIGNIN);
        }

        Member member = Member.builder()
                .memberId(memberRequest.memberId())
                .password(passwordEncoder.encode(memberRequest.password()))
                .provider(ProviderType.LOCAL)
                .email(memberRequest.email())
                .name(memberRequest.name())
                .birth(memberRequest.birth())
                .gender(memberRequest.gender())
                .authority(memberRequest.role())
                .status(ACTIVITY)
                .build();

        memberJpaRepository.save(member);

        if (memberRequest.role().equals(RoleType.MEMBER)) {

            TermsOfUse termsOfUse = TermsOfUse.builder()
                    .memberCode(member.getMemberCode())
                    .isTermsConsent(memberRequest.isTermsConsent())
                    .isFinancialConsent(memberRequest.isFinancialConsent())
                    .isPersonalDataConsent(memberRequest.isPersonalDataConsent())
                    .build();

            termsOfUseJpaRepository.save(termsOfUse);

            BigDecimal zeroPoint = BigDecimal.ZERO;
            Point point = Point.builder()
                    .memberCode(member.getMemberCode())
                    .pointBalance(zeroPoint)
                    .build();
            pointJpaRepository.save(point);

        }
    }

    public void reLogin(String memberCode, String requestMemberId, String requestPwd, ProviderType providerType) {

        Member member;
        if(requestPwd != null) {

            member = memberJpaRepository.findByMemberCode(memberCode)
                    .orElseThrow(() -> new MarkException(USER_NOT_FOUND));

            boolean memberId = member.getMemberId().equals(requestMemberId);

            boolean pwd = passwordEncoder.matches(requestPwd, member.getPassword());

            if (!memberId || !pwd) {
                throw new MarkException(INVALID_MEMBER);
            }
        } else {
            member = memberJpaRepository.findByMemberCodeAndMemberIdAndProvider(memberCode,requestMemberId ,providerType).orElseThrow(() -> new MarkException(USER_NOT_FOUND));
            boolean memberId = member.getMemberId().equals(requestMemberId);
            boolean provider = member.getProvider().equals(providerType);

            if (!memberId || !provider) {
                throw new MarkException(USER_NOT_FOUND);
            }
        }
    }

    @Transactional
    public void reissue(String refreshToken, HttpServletResponse response) {

        String cleanRefreshToken = cleanToken(refreshToken);

        Member member = memberJpaRepository.findByRefreshToken(cleanRefreshToken)
                .orElseThrow(() -> new MarkException(INVALID_REFRESHTOKEN));

        String newAccessToken;

        if(member.getRefreshEndDate().after(new Date())) {

            newAccessToken = jwtService.createAccessToken(Map.of(
                    "memberCode", member.getMemberCode(),
                    "memberRole", member.getAuthority().name()
            ));

        } else {
            throw new MarkException(EXPIRED_REFRESHTOKEN);
        }

        Date accessTokenExpiration = jwtService.extractExpiration(newAccessToken);

        response.setHeader("Access-Token", newAccessToken);
        response.setHeader("Access-Expires", String.valueOf(accessTokenExpiration.getTime()));
        response.setHeader("Refresh-Token", refreshToken);
        response.setContentType("application/json");
        response.setStatus( HttpServletResponse.SC_OK );

    }

    private String cleanToken(String token) {
        // "Bearer " 제거하고 공백 제거
        if (token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }
        return token.replace(" ", "");
    }

     /*복호화*/
    public String decryptSSOToken(String transformedToken) throws Exception {
        return EncUtils.decrypt(transformedToken, "afterSso");
    }

     /*검증*/
    public String validateSSOToken(String ssoToken) {

            // 토큰에서 사용자 정보 추출
            String memberCode = jwtService.getMemberCode(ssoToken, "sso-token")
                    .orElseThrow(() -> new MarkException(INVALID_CODE));

            Member member = memberJpaRepository.findByMemberCode(memberCode)
                    .orElseThrow(() -> new MarkException(USER_NOT_FOUND));

            if (member != null) {
                // 새로운 AccessToken과 RefreshToken 발급
                String newAccessToken = jwtService.createAccessToken(Map.of(
                        "memberCode", member.getMemberCode(),
                        "memberRole", member.getAuthority().name()
                ));
                String newRefreshToken = jwtService.createRefreshToken();

                /* 발급한 refresh token을 DB에 저장해 둔다. */
                jwtService.updateRefreshToken( memberCode, newRefreshToken );


                return newAccessToken;
            }
            return null;
    }

    /* 테스트 후 삭제 */

    public String fistCode(String transformedToken) throws Exception {

        return EncUtils.encrypt(transformedToken, "beforeSso");

    }

    public String packCode(String transformedToken) throws Exception {

        return EncUtils.decrypt(transformedToken, "beforeSso");

    }

    public String repackCode(String ssoToken) throws Exception {

        return EncUtils.encrypt(ssoToken, "afterSso");
    }
}
