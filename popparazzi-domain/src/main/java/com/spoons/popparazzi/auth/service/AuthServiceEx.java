/*
package com.spoons.popparazzi.auth.service;

import com.spoons.popparazzi.jwt.service.JwtService;
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

import java.util.Date;
import java.util.Map;

import static kr.co.hs.domain.modules.member.domain.constant.Status.ACTIVITY;
import static kr.co.hs.util.exception.type.ErrorCode.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceEx {

    private final MemberJpaRepository memberJpaRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;


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


}
*/
