package com.spoons.popparazzi.auth;

import com.spoons.popparazzi.auth.command.MemberSignupCommand;
import com.spoons.popparazzi.auth.dto.request.MemberSignupRequest;
import com.spoons.popparazzi.auth.service.MemberService;
import com.spoons.popparazzi.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
}
