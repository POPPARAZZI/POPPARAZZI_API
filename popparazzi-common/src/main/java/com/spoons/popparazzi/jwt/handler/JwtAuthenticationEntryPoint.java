package com.spoons.popparazzi.jwt.handler;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.spoons.popparazzi.error.code.CommonErrorCode;
import com.spoons.popparazzi.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/* 인증 실패 처리 */
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    /* 유효한 자격 증명(token)을 제공하지 않고 접근하려 하는 경우 인증 실패이므로 401 오류 반환 */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException{
       // response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ApiResponse<Object> errorRes = ApiResponse.error(CommonErrorCode.INVALID_TOKEN);
        response.getWriter().write(objectMapper.writeValueAsString( errorRes ));
    }
}
