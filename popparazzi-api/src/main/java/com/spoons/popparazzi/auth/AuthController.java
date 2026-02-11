package com.spoons.popparazzi.auth;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "로그인, 로그아웃, 회원가입", description = "로그인, 로그아웃, 회원가입 관리 API")
public class AuthController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello World";
    }
}
