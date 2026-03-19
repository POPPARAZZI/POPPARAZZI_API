package com.spoons.popparazzi.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.hs.util.exception.ExceptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static kr.co.hs.util.exception.type.ErrorCode.*;


@RequiredArgsConstructor
@Component
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    // 실패 했을때 호출
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        ExceptionResponse markException ;

        if (exception instanceof DisabledException) {
            markException = new ExceptionResponse(NOT_FOUND_MEMBER_NON_STATUS);
        } else if (exception instanceof BadCredentialsException) {
            markException = new ExceptionResponse(INVALID_MEMBER);
        } else if (exception instanceof UsernameNotFoundException) {
            markException = new ExceptionResponse(USER_NOT_FOUND);
        } else {
            markException = new ExceptionResponse(FAIL_LOGIN);
        }


        response.setStatus( markException.hashCode());
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        // body 내용을 출력스트림 사용해서 출력
        // java obj -> JSON String 으로 변환
        response.getWriter().write( objectMapper.writeValueAsString( markException ) );

    }
}
