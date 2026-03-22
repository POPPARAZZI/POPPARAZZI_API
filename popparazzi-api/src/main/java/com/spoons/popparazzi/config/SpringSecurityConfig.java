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
                // httpBasic 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)
                // 경로별 인가
                .authorizeHttpRequests(auth -> auth
                        // Swagger
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // Auth
                        .requestMatchers("/auth/**").permitAll()
                        // OPTIONS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated())
                //.anyRequest().permitAll())
                // CORS 필터 추가
                .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class) // ← 메서드 호출
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint())
                        .accessDeniedHandler(jwtAccessDeniedHandler()));


        return http.build();
    }

    /*
     * security 설정 시, 사용할 인코더 설정
     * */
    @Bean
    public PasswordEncoder passwordEncoder() {
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
        provider.setUserDetailsService(customUserDetailsService);         // DB에서 아이디가 맞는지 조회해오는 서비스

        return new ProviderManager( provider);

    }

    /* JWT 인증 필터 */
    // 만든 필터를 빈으로 등록
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter( jwtService , customUserDetailsService);
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

