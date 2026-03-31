package com.spoons.popparazzi.auth.dto.request;

import com.spoons.popparazzi.auth.command.MemberLoginCommand;
import com.spoons.popparazzi.auth.command.MemberSignupCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberLoginRequest {

    @NotBlank(message = "아이디는 필수입니다.")
    @Email
    private String id;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Pattern(
            regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{}|;':\",./<>?]).{10,}$",
            message = "영문, 숫자, 특수기호를 모두 포함하여 10자 이상 입력해주세요"
    )
    private String pwd;

    public MemberLoginCommand toCommand() {
        return new MemberLoginCommand(
                id,
                pwd
        );
    }
}
