package com.spoons.popparazzi.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SpringSecurityConfig {

    private final JwtService jwtService;
    private final CorsFilter corsFilter;
    private final LoginService  loginService;
    private final ObjectMapper objectMapper;
    private final DeviceService deviceService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // CSRF 설정 비활성화
        http.csrf(AbstractHttpConfigurer::disable)
                // API 서버는 session을 사용하지 않으므로 STATELESS 설정
                .sessionManagement(sessionManagementConfigurer -> sessionManagementConfigurer
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // FormLogin 비활성화
                .formLogin(AbstractHttpConfigurer::disable)
                // httpBasic 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)
                //경로별 인가 작업
                .authorizeHttpRequests((auth) -> auth
                        // Swagger 관련 경로 허용
                        .antMatchers("/api/swagger-ui/**", "/api/v3/api-docs/**").permitAll()
                        .antMatchers("/api/login", "/api/signup", "/api/logout").permitAll()
                        .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated())
                //.anyRequest().permitAll())
                // 로그인 필터 설정
                .addFilterBefore(customUsernamePasswordAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                // JWT Token 인증 필터
                .addFilterBefore( jwtAuthenticationFilter(), CustomUsernamePasswordAuthenticationFilter.class )
                // CORS 필터 추가
                .addFilterBefore(corsFilter, JwtAuthenticationFilter.class)
                // Exception handling
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint()) // 인증 실패
                        .accessDeniedHandler(jwtAccessDeniedHandler()));       // 인가 실패


        return http.build();
    }

    /*
     * security 설정 시, 사용할 인코더 설정
     * */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /* 인증 매니저 빈 등록 => 로그인 시 사용할 password encode 설정,
     * 로그인 시 유저 조회하는 메소드를 가진 Service 클래스 설정
     * ----------------------------------------------
     * 밑에 필터가 사용할 인증 매니저 (아이디와 비밀번호 체킹을 여기서 한다)
     * */
    @Bean
    public AuthenticationManager authenticationManager() {

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder( passwordEncoder() );       // 비밀번호 맞는지 체크 (인코더 사용해서)
        provider.setUserDetailsService(loginService);         // DB에서 아이디가 맞는지 조회해오는 서비스

        return new ProviderManager( provider);

    }

    /* 로그인 실패 핸들러 빈 등록 */
    @Bean
    public LoginFailureHandler loginFailureHandler() {
        return new LoginFailureHandler();
    }

    /* 로그인 성공 핸들러 빈 등록
     * 토큰 생성을 위해서 jwtService를 넘겨줘야한다. */
    @Bean
    public LoginSuccessHandler loginSuccessHandler() {
        return new LoginSuccessHandler( jwtService, deviceService);
    }

    /* 로그인 필터 빈 등록 */
    /* CustomUsernamePasswordAuthenticationFilter 만든것을 빈 등록
     * 위쪽의 authenticationManager로 이동 */
    @Bean
    public CustomUsernamePasswordAuthenticationFilter customUsernamePasswordAuthenticationFilter() {

        CustomUsernamePasswordAuthenticationFilter customUsernamePasswordAuthenticationFilter
                = new CustomUsernamePasswordAuthenticationFilter( objectMapper, loginFailureHandler() );

        /* 사용할 인증 매니저 설정 (상단 매니저 적용) */
        customUsernamePasswordAuthenticationFilter.setAuthenticationManager( authenticationManager() );

        /* 로그인 실패 핸들링 */
        customUsernamePasswordAuthenticationFilter.setAuthenticationFailureHandler( loginFailureHandler() );

        /* 로그인 성공 핸들링
         * 성공시 성공핸들링에서 액세스토큰, 리프레쉬 토큰 생성, DB에 저장 동작함.
         * */
        customUsernamePasswordAuthenticationFilter.setAuthenticationSuccessHandler( loginSuccessHandler() );

        return customUsernamePasswordAuthenticationFilter;

    }


    /* JWT 인증 필터 */
    // 만든 필터를 빈으로 등록
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter( jwtService );
    }


    /* 인증 실패 핸들러 */
    @Bean
    public JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint () {

        return new JwtAuthenticationEntryPoint( objectMapper );
    }

    /* 인가 실패 핸들러 */
    @Bean
    public JwtAccessDeniedHandler jwtAccessDeniedHandler() {

        return new JwtAccessDeniedHandler( objectMapper );
    }



}

