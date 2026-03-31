/*
package com.spoons.popparazzi.auth.filter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spoons.popparazzi.auth.service.SocialAuthServiceFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

*/
/* 스프링 시큐리티의 기존 UsernamePasswordAuthenticationFilter를 대처할 Custom Filter 작성 *//*


public class CustomUsernamePasswordAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    // POST방식으로 /api/auth/login 요청으로 왔을때 이 필터가 동작
    private static final String HTTP_MEHOD = "POST";
    private static final String LOGIN_REQUEST_URL = "/auth/login";
    private static final String SOCIAL_LOGIN_REQUEST_URL = "/auth/social-login";
    private static final String CONTENT_TYPE = "application/json";

    private static final String USERNAME = "memberId";
    private static final String PASSWORD = "memberPassword";

    private final ObjectMapper objectMapper;
    private final SocialAuthServiceFactory authServiceFactory;
    private final LoginFailureHandler loginFailureHandler;


    public CustomUsernamePasswordAuthenticationFilter(ObjectMapper objectMapper, SocialAuthServiceFactory authServiceFactory, LoginFailureHandler loginFailureHandler) {
        */
/* POST 요청이 올 때 해당 필터가 동작하도록 설정 *//*

        super(new OrRequestMatcher(
                new AntPathRequestMatcher(LOGIN_REQUEST_URL, HTTP_MEHOD),
                new AntPathRequestMatcher(SOCIAL_LOGIN_REQUEST_URL, HTTP_MEHOD)
        ));
        this.objectMapper = objectMapper;
        this.authServiceFactory = authServiceFactory;
        this.loginFailureHandler = loginFailureHandler;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException {

        if (request.getRequestURI().equals("/api/v1/auth/login")) {
            try {
                return attemptNormalAuthentication(request, response);
            } catch (AuthenticationException e) {
                // 로그인 실패 시 LoginFailureHandler를 호출하여 처리
                loginFailureHandler.onAuthenticationFailure(request, response, e);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        } else if (request.getRequestURI().equals("/api/v1/auth/social-login")) {
            try {
                return attemptSocialAuthentication(request, response);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }
        return null;
    }

    */
/* /api/auth/login 요청 발생 시 메소드 호출되며 인증 처리 작성 *//*

    public Authentication attemptNormalAuthentication(HttpServletRequest request, HttpServletResponse response) throws Exception {

        // 요청 컨텐츠 타입이 알맞은지 확인
        // Request Content Type "application/json" 확인
        if (request.getContentType() == null || !request.getContentType().equals(CONTENT_TYPE)) {

            throw new AuthenticationServiceException("Content-Type not supported");
        }

        // Request Body 읽어오기
        // ex) "{ 'memberId' : 'test01', 'memberPassword' : '1234' }"
        String body = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);


        // JSON 문자열을 Java Map 타입으로 변환
        // body를 읽어와서 map.class 타입으로 변환하겠다.
        // username, password 타입으로 있을것
        Map<String, String> bodyMap = objectMapper.readValue(body, new TypeReference<>() {
        });


        // key 값을 전달해서 Map에서 id와 pwd 꺼내기
        String memberId = bodyMap.get(USERNAME);
        String memberPassword = bodyMap.get(PASSWORD);

        // 인증 토큰에 세팅
        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(memberId, memberPassword);

        authenticationToken.setDetails(ProviderType.LOCAL);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        //  Spring Security 인증 처리
        Authentication authResult = this.getAuthenticationManager().authenticate(authenticationToken);

        //  ROLE 확인 (Spring Security에서 자동 매핑됨)
        // web, bos 같은 api 사용.. 나중에 시큐리티로 관리할 수 있게 수정해야 함.
        Collection<? extends GrantedAuthority> authorities = authResult.getAuthorities();
        boolean isAdmin = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isMember = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_MEMBER"));

        //  X-User-Type (BOS or WEB) 확인
        String userType = request.getHeader("X-User-Type");
        if (userType == null) {
            throw new AuthenticationServiceException("Missing user type");
        }

        //  권한 검증 및 차단
        if ("BOS".equalsIgnoreCase(userType) && !isAdmin) {
            throw new AccessDeniedException("BOS (ADMIN) users are not allowed in WEB");
        }
        if ("WEB".equalsIgnoreCase(userType) && !isMember) {
            throw new AccessDeniedException("WEB (MEMBER) users are not allowed in BOS");
        }

        return authResult;

    }

    private Authentication attemptSocialAuthentication(HttpServletRequest request, HttpServletResponse response) throws Exception {

        // 소셜 로그인 로직 구현
        String body = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
        SocialRequestDTO socialRequest = objectMapper.readValue(body, SocialRequestDTO.class);

        SocialAuthService socialService = authServiceFactory.getService(socialRequest.getProvider());

        SocialUserInfo userInfo;

        if (socialRequest.getProvider() == APPLE) {
            userInfo = socialService.getUserInfo(socialRequest.getIdToken());
        } else {
            userInfo = socialService.getUserInfo(socialRequest.getAccessToken());
        }

        Member member = authServiceFactory.findByMemberIdAndProvider(userInfo.getEmail(), userInfo.getProvider());

        if (member == null) {

            // 예외 처리 후 JSON 응답 반환
            response.setContentType(CONTENT_TYPE); // 응답 콘텐츠 타입을 JSON으로 설정

            PrintWriter writer = response.getWriter();

            Member hasMember = authServiceFactory.existsByMember(userInfo.getEmail());
            if (hasMember != null) {
                String provider = hasMember.getProvider() == LOCAL ? "일반 회원" : String.valueOf(hasMember.getProvider());
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                response.getWriter().write("{\"message\": \"" + provider + "로 이미 가입되어있습니다.\"}");
                writer.flush();
                return null;
            } else {
                // 회원가입
                authServiceFactory.signup(userInfo);
            }
        }

        member = authServiceFactory.findByMemberIdAndProvider(userInfo.getEmail(), userInfo.getProvider());
        ProviderType provider = member.getProvider();

        if (provider == socialRequest.getProvider()) {

            String memberId = member.getMemberId();
            String memberPassword = "SOCIAL_USER";

            // 인증 토큰에 세팅
            UsernamePasswordAuthenticationToken authenticationToken
                    = new UsernamePasswordAuthenticationToken(memberId, memberPassword);

            authenticationToken.setDetails(provider);

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            // 인증 매니저에게 인증 토큰 전달
            return this.getAuthenticationManager().authenticate(authenticationToken);

        }
        return null;
    }

}
*/
