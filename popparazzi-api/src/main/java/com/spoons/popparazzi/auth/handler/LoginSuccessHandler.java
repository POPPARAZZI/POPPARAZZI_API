package com.spoons.popparazzi.auth.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.hs.domain.modules.memberAuth.application.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;

    // 로그인 성공시 호출, 성공시 Authentication 을 받아온다.
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException{

        // 로그인 성공 후 저장 된 인증 객체에서 정보를 꺼낸다.
        Map<String, String> memberInfo = getMemberInfo( authentication );

        // 토큰 생성
        String accessToken = jwtService.createAccessToken(memberInfo);
        String refreshToken = jwtService.createRefreshToken();

        String userType = request.getHeader("X-User-Type");

        if ("WEB".equalsIgnoreCase(userType) ) {
            // SSO 토큰 생성
            String ssoToken = jwtService.createSSOToken(memberInfo);

            // 앱으로 리다이렉트
            response.setHeader("Sso-Token", ssoToken);

        }
  
        /* 응답 헤더에 발급 된 토근을 담는다. */
        response.setHeader( "Access-Token", accessToken );

        response.setContentType("application/json");
        response.setStatus( HttpServletResponse.SC_OK );

        /* 발급한 refresh token을 DB에 저장해 둔다. */
        jwtService.updateRefreshToken( memberInfo.get("memberCode"), refreshToken );

        // JSON 형식으로 응답 바디에 메시지를 작성
        PrintWriter writer = response.getWriter();
        writer.write("{\"message\": \"로그인 성공\" }");
        writer.flush();

    }

    private Map<String, String> getMemberInfo( Authentication authentication ) {

        // authentication 안에는 UserDetails 가 들어있다.

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String memberRole = userDetails.getAuthorities()
                .stream().map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining());

        // joining 한다라는 표현은 배열에 있는 데이터들을 조인잉한다.

        // 인증객체로부터 id,role 정보를 뽑아낸다.
        // 액세트 토큰 페이로드에 보낼 정보들 (민감한 정보들은 피해라)
        return Map.of(
                "memberCode", userDetails.getUsername(),
                "memberRole", memberRole
        );

    }
}
