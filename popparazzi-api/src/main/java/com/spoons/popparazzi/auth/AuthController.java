package com.spoons.popparazzi.auth;

import com.spoons.popparazzi.auth.command.MemberLoginCommand;
import com.spoons.popparazzi.auth.command.MemberSignupCommand;
import com.spoons.popparazzi.auth.command.TokenResult;
import com.spoons.popparazzi.auth.dto.request.MemberLoginRequest;
import com.spoons.popparazzi.auth.dto.request.MemberSignupRequest;
import com.spoons.popparazzi.auth.dto.response.TokenResponse;
import com.spoons.popparazzi.auth.entity.enums.SnsType;
import com.spoons.popparazzi.auth.service.AuthService;
import com.spoons.popparazzi.jwt.security.CustomUserDetails;
import com.spoons.popparazzi.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "로그인, 로그아웃, 회원가입", description = "로그인, 로그아웃, 회원가입 관리 API")
public class AuthController {

    private final AuthService authService;

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
        authService.signup(memberCommand);

        return ApiResponse.success();
    }

    /**
     *
     * @methodName	: login
     * @author		: inseon kang
     * @date		: 2026.03.22
     * @returnType	: ApiResponse
     * @desc		: 로그인
     */
    @PostMapping("/login")
    public ApiResponse<?> login(@RequestBody @Valid MemberLoginRequest memberLoginRequest) {

        MemberLoginCommand memberCommand = memberLoginRequest.toCommand();
        TokenResult result = authService.login(memberCommand);

        return ApiResponse.success(TokenResponse.from(result));
    }


    /*  액세스토큰 재발급 */
/*    @PostMapping("/reissue")
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

    }*/
}
