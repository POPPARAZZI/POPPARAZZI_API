package com.spoons.popparazzi.jwt.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spoons.popparazzi.error.code.CommonErrorCode;
import com.spoons.popparazzi.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

/* 인가 처리 실패 */
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;


    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        // 필요한 권한 없이 접근할려 할때 403
       // response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ApiResponse<Object> errorRes = ApiResponse.error(CommonErrorCode.FORBIDDEN);
        response.getWriter().write(objectMapper.writeValueAsString( errorRes )); }
}

