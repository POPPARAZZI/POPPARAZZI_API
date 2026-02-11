package com.spoons.popparazzi.jwt.filter;


import com.spoons.popparazzi.jwt.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        try {

            /* 로그인 요청의 경우 다음 필터(로그인 필터)로 진행 */
            /* if문 처리에 걸리지 않았다면 로그인 처리가 아니다. */
            if (requestURI.equals("/api/login") ) {

                filterChain.doFilter(request, response);
                return;
            }

            /* --------------- 로그인 요청이 아닌 유효성 검사 일경우 아래 구문 진행 ----------------------------- */

            // 1. 사용자 헤더에서 Refresh Token 추출
            String refreshToken = jwtService.getRefreshToken(request)
                    .filter(token -> jwtService.isAliveToken(token, true) == 1)
                    .orElse(null);

            // 2-1 Refresh Token 이 있다면?
            /*
             * AccessToken 만료 상황
             * DB에서 Refresh Token 일치 여부 확인 후 일치하면 AccessToken 재발급
             * */
            if (requestURI.equals("/api/refresh") && refreshToken != null) {
                jwtService.checkRefreshTokenAndReIssueAccessToken(response, refreshToken, request);
            } else {
                jwtService.checkAccessTokenAndAuthentication(request, response, filterChain);
            }

        } catch (AuthenticationException e) {

            log.error("필터 처리 중 예외 발생", e);

        }

    }
}
