package com.spoons.popparazzi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spoons.popparazzi.auth.service.SocialAuthServiceFactory;
import com.spoons.popparazzi.jwt.filter.JwtAuthenticationFilter;
import com.spoons.popparazzi.jwt.handler.JwtAccessDeniedHandler;
import com.spoons.popparazzi.jwt.handler.JwtAuthenticationEntryPoint;
import com.spoons.popparazzi.jwt.service.CustomUserDetailsService;
import com.spoons.popparazzi.jwt.service.JwtService;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SpringSecurityConfig {

    private final JwtService jwtService;
    private final CorsFilter corsFilter;
    private final CustomUserDetailsService customUserDetailsService;
    private final ObjectMapper objectMapper;
    private final SocialAuthServiceFactory authServiceFactory;
    // private final DeviceService deviceService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // CSRF 설정 비활성화
        http.csrf(AbstractHttpConfigurer::disable)

                // API 서버는 session을 사용하지 않으므로 STATELESS 설정
                .sessionManagement(sessionManagementConfigurer -> sessionManagementConfigurer
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // FormLogin 비활성화
                .formLogin(AbstractHttpConfigurer::disable)

                // httpBasic 비활성화 (JWT 사용하기 때문)
                .httpBasic(AbstractHttpConfigurer::disable)

                // 경로별 인가
                .authorizeHttpRequests(auth -> auth
                        // Swagger
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // Auth (회원가입 / 로그인)
                        .requestMatchers("/auth/**").permitAll()

                        // 기존 SecurityConfig에서 열어둔 경로(임시)
                        .requestMatchers("/moim/**").permitAll()
                        .requestMatchers("/likes/**").permitAll()
                        .requestMatchers("/board/**").permitAll()
                        .requestMatchers("/test/**").permitAll()
                        .requestMatchers("/files/**").permitAll()

                        // Preflight 요청 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // CORS 필터 추가
                .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)

                // JWT 인증 필터 추가 (로그인 제외 모든 요청에서 토큰 검사)
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)

                // 인증 / 인가 실패 처리
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint())
                        .accessDeniedHandler(jwtAccessDeniedHandler())
                );

        return http.build();
    }

    /*
     * security 설정 시, 사용할 인코더 설정
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*
     * 인증 매니저 빈 등록
     * 로그인 시 사용할 password encode 설정,
     * 로그인 시 유저 조회하는 Service 설정
     * ----------------------------------------------
     * 아이디와 비밀번호 검증을 담당
     */
    @Bean
    public AuthenticationManager authenticationManager() {

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());               // 비밀번호 검증
        provider.setUserDetailsService(customUserDetailsService);     // 유저 조회

        return new ProviderManager(provider);
    }

    /*
     * JWT 인증 필터
     * Request마다 토큰을 검사하여 SecurityContext에 인증 정보 세팅
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtService, customUserDetailsService);
    }

    /*
     * 인증 실패 핸들러 (401)
     */
    @Bean
    public JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint () {
        return new JwtAuthenticationEntryPoint(objectMapper);
    }

    /*
     * 인가 실패 핸들러 (403)
     */
    @Bean
    public JwtAccessDeniedHandler jwtAccessDeniedHandler() {
        return new JwtAccessDeniedHandler(objectMapper);
    }
}