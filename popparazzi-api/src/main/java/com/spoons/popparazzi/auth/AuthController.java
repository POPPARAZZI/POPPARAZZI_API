package com.spoons.popparazzi.auth;

import com.spoons.popparazzi.auth.command.MemberSignupCommand;
import com.spoons.popparazzi.auth.dto.request.MemberSignupRequest;
import com.spoons.popparazzi.auth.service.MemberService;
import com.spoons.popparazzi.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController("/member")
@RequiredArgsConstructor
@Tag(name = "로그인, 로그아웃, 회원가입", description = "로그인, 로그아웃, 회원가입 관리 API")
public class AuthController {

    private final MemberService memberService;

    /**
     *
     * @methodName	: signup
     * @author		: inseon kang
     * @date		: 2026.03.18
     * @returnType	: ApiResponse
     * @desc		: 회원가입
     */
    @PostMapping("/signup")
    public ApiResponse<Void> signup(@RequestBody @Valid MemberSignupRequest memberSignupRequest) {

        MemberSignupCommand memberCommand = memberSignupRequest.toCommand();
        memberService.signup(memberCommand);

        return ApiResponse.success();
    }

    /* 재로그인 (정보 수정 전 확인) */
    @PostMapping("/re-login")
    public ResponseEntity<HttpResData<?>> reLogin(@RequestBody ReLoginVO memberRequest) {
        String memberCode = MemberData.build();

        if (memberCode != null) {

            authService.reLogin(memberCode, memberRequest.memberId(), memberRequest.memberPassword(), null );

            return ResponseEntity.ok(HttpResData.success("확인되었습니다."));

        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(HttpResData.error(HttpStatus.UNAUTHORIZED.value(), "로그인 해주세요."));
        }
    }

    /*  액세스토큰 재발급 */
    @PostMapping("/reissue")
    public ResponseEntity<HttpResData<?>> reissue(HttpServletRequest request, HttpServletResponse response) {

        try {

            // 쿠키에서 Refresh Token을 추출
            // String refreshToken = extractRefreshTokenFromCookies(request);
            String refreshToken = request.getHeader("Refresh-Token" );

            if(refreshToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(HttpResData.error(HttpStatus.UNAUTHORIZED.value(), "Refresh Token이 존재하지 않습니다."));
            }

            authService.reissue(refreshToken, response);
            return ResponseEntity.ok(HttpResData.success("AccessToken 재 발급됐습니다."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(HttpResData.error(HttpStatus.UNAUTHORIZED.value(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(HttpResData.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error"));
        }

    }
}
