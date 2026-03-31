package com.spoons.popparazzi.jwt.filter;

import com.spoons.popparazzi.jwt.service.CustomUserDetailsService;
import com.spoons.popparazzi.jwt.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


/* Request에서 Token을 꺼내서 인증 확인하는 필터
* (로그인 외에 인증이 필요한 요청들을 처리) */
// OncePerRequestFilter 딱 한번만 필터를 통과한다.
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {


    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // /auth/** 는 필터 통과 (SecurityConfig에서 설정)
        String accessToken = jwtService.getAccessToken(request).orElse(null);

        int isValidToken = jwtService.isAliveToken(accessToken, false);

        if (accessToken != null && isValidToken == 1) {
            // SecurityContext에 인증 정보 세팅만
            jwtService.extractUuid(accessToken)
                    .ifPresent(uuid -> saveAuthentication(uuid));
        }

        filterChain.doFilter(request, response);

    }

    private void saveAuthentication(String memberUuid) {

        UserDetails userDetails = customUserDetailsService
                .loadUserByMemberUuid(memberUuid);  // DB 조회

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication); // SecurityContext에 등록
    }

}
