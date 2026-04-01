package com.spoons.popparazzi.jwt.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spoons.popparazzi.auth.repository.AuthJpaRepository;
import com.spoons.popparazzi.error.exception.BusinessException;
import com.spoons.popparazzi.error.exception.UnauthorizedException;
import com.spoons.popparazzi.member.entity.Member;
import com.spoons.popparazzi.response.ApiResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.Key;
import java.security.SignatureException;
import java.util.*;

import static com.spoons.popparazzi.error.code.CommonErrorCode.MEMBER_NOT_FOUND;

@Slf4j
@Service
public class JwtService {

    @Value("${jwt.access-token-expiration}")
    private  long accessTokenExpirationPeriod;
    @Value("${jwt.refresh-token-expiration}")
    private  long refreshTokenExpirationPeriod;

    private final Key SECRET_KEY;
    private final Key REFRESH_SECRET_KEY;

    private final AuthJpaRepository authJpaRepository;
    //private final DeviceService deviceService;

    private static final String ACCESS_TOKEN_SUBJECT = "Authorization";
    private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
    private static final String BEARER = "Bearer ";



    public JwtService(@Value("${jwt.secret}") String secretKey, @Value("${jwt.refresh-secret}") String refreshSecretKey,
                      AuthJpaRepository authJpaRepository) {
        this.authJpaRepository = authJpaRepository;
       // this.deviceService = deviceService;

        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        byte[] refreshKeyBytes = Decoders.BASE64.decode(refreshSecretKey);

        this.SECRET_KEY = Keys.hmacShaKeyFor(keyBytes);
        this.REFRESH_SECRET_KEY = Keys.hmacShaKeyFor(refreshKeyBytes);

    }

    public String createAccessToken(Member member) {

        // Claims 쪽에 키, 밸류 값으로 얼마든지 저장
        Claims claims = Jwts.claims().setSubject( ACCESS_TOKEN_SUBJECT );

        claims.put("memberUuid", member.getMemberUuid()); // 토큰 식별용
        claims.put("role", member.getRole());
        claims.put("provider", member.getSnsType().name());

        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setClaims( claims )
                .setIssuedAt(  new Date() )
                .setExpiration( new Date(System.currentTimeMillis() + accessTokenExpirationPeriod))
                .signWith( SECRET_KEY, SignatureAlgorithm.HS512 )
                .compact();

    }

    public String createRefreshToken() {

        // access 토큰과 맥락은 비슷하다.
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject( REFRESH_TOKEN_SUBJECT )
                .setIssuedAt(  new Date() )
                .setExpiration( new Date(System.currentTimeMillis() + refreshTokenExpirationPeriod) )
                .signWith( REFRESH_SECRET_KEY, SignatureAlgorithm.HS512 )
                .compact();

    }

    @Transactional
    public void updateRefreshToken( String memberUuid, String refreshToken) {

        Member member = authJpaRepository.findByMemberUuid(memberUuid)
                .orElseThrow(() -> new BusinessException(MEMBER_NOT_FOUND));

        // 멤버가 존재하면 refreshToken 업데이트
        member.updateToken(refreshToken);

    }


    /* 넘어온 refreshToken을 꺼낸다. */
    public Optional<String> getRefreshToken(HttpServletRequest request ) {

        return Optional.ofNullable( request.getHeader( "Refresh-Token" ) )
                // refresh 토큰이 없다면 null 반환, 있다면 문자열 반환. (가공처리)
                // jwt의 앞쪽 형태인 BEARER 인지 필터해봄 (아닐경우 false로 null값을 반환 맞다면 true 문자열 반환)
                .filter( refreshToken -> refreshToken.startsWith( BEARER ) )
                // 들어온 문자열에서 BEARER 을 ""으로 가공
                .map( refreshToken -> refreshToken.replace(BEARER, "") );

    }

    public Optional<String> getAccessToken( HttpServletRequest request ) {


        return Optional.ofNullable( request.getHeader( "Authorization" ) )
                // Access 토큰이 없다면 null 반환, 있다면 문자열 반환. (가공처리)
                .filter( accessToken -> accessToken.startsWith( BEARER ) )
                .map( accessToken -> accessToken.replace( BEARER, "") );
    }

    // 토큰의 유효성 검사 하는 메소드
    public int isAliveToken( String tokenKey, boolean isRefreshToken ) {

        Jws<Claims> token;

        try {

            token = getClaims(tokenKey, isRefreshToken);

            if(token == null) {
                return 0;
            }

        } catch ( AuthenticationException e ) {
            // exception 발생시키면서 false를 반환.
            log.info("유효하지 않은 토큰입니다. {}", e.getMessage());
            return -3;

        }catch (SignatureException e) {
            log.info("jwt 암호화 키가 다릅니다.");
            return -2;

        } catch (UnauthorizedException e) {
            log.info("token 키가 만료되었습니다.");
            return -1;

        } catch (IllegalArgumentException e) {
            log.info("jwt 키가 존재하지 않습니다.");

            return 0;

        }

        boolean result = validateToken(token);

        if (result) {
            return 1;
        } else {
            return -1;
        }

    }

    public Jws<Claims> getClaims(String tokenKey, boolean isRefreshToken)
            throws SignatureException, IllegalArgumentException {

        if (!isRefreshToken) {
            return Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(tokenKey);
        } else {
            return Jwts.parserBuilder().setSigningKey(REFRESH_SECRET_KEY).build().parseClaimsJws(tokenKey);
        }

    }

    // 토큰 검증 함수
    public boolean validateToken(Jws<Claims> claims) {
        // 토큰 만료 시간이 현재 시간을 지났는지 검증
        return !claims.getBody().getExpiration().before(new Date());
    }

    public void checkRefreshTokenAndReIssueAccessToken(HttpServletResponse response, String refreshToken, HttpServletRequest request ) throws IOException {

        Member member = authJpaRepository.findByToken(refreshToken);

        if (member != null) {

            // 리프레시 토큰이 새로 발행?
            String reIssuedRefreshToken = reIssuedRefreshToken(member.getMemberUuid());

            // 일치한다면 새로운 액세스 토큰을 발급
            String newAccessToken = createAccessToken(member);

            // 사용자 응답쪽으로 넘긴다.

            response.setStatus( HttpServletResponse.SC_OK );
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=UTF-8");

            Map<String, Object> data = new HashMap<>();
            data.put("Authorization", newAccessToken);
            data.put("RefreshToken", reIssuedRefreshToken);

            ObjectMapper objectMapper = new ObjectMapper();

            ApiResponse<Map<String, Object>> output = ApiResponse.success(data);

            // JSON 형식으로 응답 바디에 메시지를 작성
            PrintWriter writer = response.getWriter();
            writer.write(objectMapper.writeValueAsString(output));
            writer.flush();

        } else {
            throw new BusinessException(MEMBER_NOT_FOUND);
        }

    }

    public String reIssuedRefreshToken( String memberUuid ) {

        // 새로운 리프래쉬 토큰을 만들었다.
        String reIssuedRefreshToken = createRefreshToken();
        Date refreshEndDate = new Date(System.currentTimeMillis() + refreshTokenExpirationPeriod);
        //
        updateRefreshToken(memberUuid, reIssuedRefreshToken);

        return reIssuedRefreshToken;

    }


    // 액세스 토큰을 전달하여 memberCode를 꺼내오는 기능
    public Optional<String> extractUuid(String token) {

        try {
            return Optional.ofNullable(
                    Jwts.parserBuilder()
                            // 파싱처리할수 있는 객체 먼저 소환
                            .setSigningKey( SECRET_KEY )
                            // 파싱할때 서명키를 전달
                            .build()
                            // 파싱을 할수 있도록
                            .parseClaimsJws( token )
                            // 파싱해오는 동작
                            .getBody()
                            .get("memberUuid", String.class)
            );

        } catch ( Exception e ) {

            log.info("Token이 유효하지 않습니다.");
            return Optional.empty();            // 유효하지 않을 경우 비워서 보낸다.
        }

    }

    public Date extractExpiration(String accessToken) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)  // jwtToken은 토큰 서명에 사용한 키입니다.
                .build()
                .parseClaimsJws(accessToken)
                .getBody();
        return claims.getExpiration();
    }


}


