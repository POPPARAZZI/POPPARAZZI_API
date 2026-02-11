package com.spoons.popparazzi.jwt.service;

import com.spoons.popparazzi.auth.repository.MemberRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.io.PrintWriter;
import java.security.Key;
import java.security.SignatureException;
import java.util.*;

@Slf4j
@Service
public class JwtService {

    @Value("${jwt.access-token-expiration}")
    private  long accessTokenExpirationPeriod;
    @Value("${jwt.refresh-token-expiration}")
    private  long refreshTokenExpirationPeriod;

    private final Key SECRET_KEY;
    private final Key REFRESH_SECRET_KEY;

    private final MemberRepository memberRepository;
    private final DeviceService deviceService;

    private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
    private static final String BEARER = "Bearer ";



    public JwtService(@Value("${jwt.secret}") String secretKey, @Value("${jwt.secret-secret}") String refreshSecretKey,
                      MemberRepository memberRepository, DeviceService deviceService) {
        this.memberRepository = memberRepository;
        this.deviceService = deviceService;

        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        byte[] refreshKeyBytes = Decoders.BASE64.decode(refreshSecretKey);

        this.SECRET_KEY = Keys.hmacShaKeyFor(keyBytes);
        this.REFRESH_SECRET_KEY = Keys.hmacShaKeyFor(refreshKeyBytes);

    }

    public String createAccessToken(Map<String, String> memberInfo) {

        // Claims 쪽에 키, 밸류 값으로 얼마든지 저장
        Claims claims = Jwts.claims().setSubject( ACCESS_TOKEN_SUBJECT );

        claims.putAll( memberInfo );

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
    public void updateRefreshToken( String uId, String refreshToken) {

        Date refreshEndDate = new Date(System.currentTimeMillis() + refreshTokenExpirationPeriod);

        int user = userDAO.findByMember(uId);

        if (user > 0) {
            // 멤버가 존재하면 refreshToken 업데이트
            userDAO.updateRefreshToken(uId, refreshToken, refreshEndDate);
        } else {
            // 멤버가 존재하지 않으면 예외 발생
            throw new NotFoundUserException();
        }

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

        } catch (ExpiredJwtException e) {
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
            throws SignatureException, ExpiredJwtException, IllegalArgumentException {

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

        HashMap<String, Object> user = userDAO.findByRefreshToken(refreshToken);

        if (user != null) {

          /*  String dType;

            try {
                JSONObject jsonObj = new JSONObject(IOUtils.toString(request.getReader()));

                dType = (String) jsonObj.get("dType");
            } catch(JSONException e) {

                throw new DeviceInfoException();

            }*/

            String uId = user.get("uId").toString();
            // 리프레시 토큰이 새로 발행?
            String reIssuedRefreshToken = reIssuedRefreshToken(uId);

            // 일치한다면 새로운 액세스 토큰을 발급
            String newAccessToken = createAccessToken(
                    Map.of("uId", uId, "spCode", (String) user.get("spCode") , "level", String.valueOf(user.get("level")))
            );

            // 사용자 응답쪽으로 넘긴다.

            response.setStatus( HttpServletResponse.SC_OK );
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=UTF-8");

            HashMap<String, Object> output = ApiErrorCode.getMsg(ApiErrorCode.SUCCESS.getResult());

            Map<String, Object> data = new HashMap<>();
            data.put("Authorization", newAccessToken);
            data.put("RefreshToken", reIssuedRefreshToken);
            output.put("data", data);

            // JSON 형식으로 응답 바디에 메시지를 작성
            PrintWriter writer = response.getWriter();
            writer.write(JsonUtil.getJsonStringFromMap(output).toJSONString());
            writer.flush();

        } else {
            throw new NotFoundUserException();
        }

    }

    public String reIssuedRefreshToken( String uId ) {

        // 새로운 리프래쉬 토큰을 만들었다.
        String reIssuedRefreshToken = createRefreshToken();
        Date refreshEndDate = new Date(System.currentTimeMillis() + refreshTokenExpirationPeriod);
        //
        userDAO.updateRefreshToken(uId, reIssuedRefreshToken, refreshEndDate);

        return reIssuedRefreshToken;

    }


    public void checkAccessTokenAndAuthentication( HttpServletRequest request,
                                                   HttpServletResponse response, FilterChain filterChain ) throws ServletException, IOException {

        String deviceCode = request.getHeader("X-Device-Code");

        // 유효한지 검사 // 유효하다면,
        // ifPresent 있다면? 없다면? 쓸때
        try {

            getAccessToken(request)
                    .filter(token -> this.isAliveToken(token, false) == 1)  // 유효한 토큰 필터링
                    .flatMap(this::getMemberId)  // Optional<String> 반환 → flatMap 사용
                    .map(userDAO::findByMemberId) // HashMap<String, Object> 반환 → map 사용
                    .ifPresent(userInfo -> {
                        this.saveAuthentication(userInfo);
                        // userInfo에서 uCode 추출해서 디바이스 검증
                        // 디바이스 해제가 아닐 때만 디바이스 검증
                        if (!request.getRequestURI().equals("/api/devices/unregister")) {
                            String uCode = (String) userInfo.get("uCode");
                            deviceService.validateDevice(uCode, deviceCode);
                        }
                    });

            // 다음 필터로 넘어가라
            filterChain.doFilter(request, response);
        } catch (DeviceInfoException e) {
            sendApiErrorResponse(response);
            return;
        }

    }
    private void sendApiErrorResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.OK.value()); // HTTP 200으로 설정
        response.setContentType("application/json;charset=UTF-8");


        HashMap<String, Object> output = ApiErrorCode.getMsg(ApiErrorCode.DEVICE_VALIDATION_FAILED.getResult());
        response.getWriter().write(JsonUtil.getJsonStringFromMap(output).toJSONString());
    }

    // 액세스 토큰을 전달하여 memberCode를 꺼내오는 기능
    public Optional<String> getMemberId(String token) {

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
                            .get("uId").toString()
                    // memberCode를 꺼내오겠다.
            );

        } catch ( Exception e ) {

            log.info("Token이 유효하지 않습니다.");
            return Optional.empty();            // 유효하지 않을 경우 비워서 보낸다.
        }

    }


    private void saveAuthentication( HashMap<String, Object> user ) {

        UserDetails userDetails = User.builder()
                .username( user.get("uId").toString() )
                .password( user.get("pwd").toString())
                .roles("USER")
                .build();

        CustomUser customUser = CustomUser.of((String) user.get("uCode"), (String) user.get("spCode"),  userDetails, (int) user.get("level"), (String) user.get("adultHiddenYn"));

        // userDetails.getAuthorities() User,ADMIN인지 확인
        // 1. principal(인증주체)
        // 2. credntials(비밀번호)- 로그인처리를 하는게 아니기 때문에 굳이 여기선 필요없다.
        // 3. 인증처리후 인가처리 해야하는데 user인지 admin인지 셋팅
        Authentication authentication
                = new UsernamePasswordAuthenticationToken( customUser, null, userDetails.getAuthorities() );

        // 인증객체를 저장해줘야 하는 형태가 있다.(Authentication - UsernamePasswordAuthenticationToken)
        SecurityContextHolder.getContext().setAuthentication( authentication );
    }


    public Date extractExpiration(String accessToken) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)  // jwtToken은 토큰 서명에 사용한 키입니다.
                .build()
                .parseClaimsJws(accessToken)
                .getBody();
        return claims.getExpiration();
    }

    public Map<String, Object> getUserInfo(String uId) {
        return userDAO.getUserInfo(uId);
    }
}


