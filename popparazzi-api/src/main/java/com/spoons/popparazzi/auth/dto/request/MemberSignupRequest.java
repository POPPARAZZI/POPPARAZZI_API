package com.spoons.popparazzi.auth.dto.request;

import com.spoons.popparazzi.auth.command.MemberSignupCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberSignupRequest {

    @NotBlank(message = "이메일은 필수입니다.")
    @Email
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Pattern(
            regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{}|;':\",./<>?]).{10,}$",
            message = "영문, 숫자, 특수기호를 모두 포함하여 10자 이상 입력해주세요"
    )
    private String pwd;

    @NotBlank(message = "닉네임은 필수입니다.")
    @Pattern(
            regexp = "^[a-zA-Z0-9가-힣]{2,10}$",
            message = "특수기호 없이 2자 이상 10자 이하로 입력해주세요"
    )
    private String nickName;

    @NotBlank(message = "성별은 필수입니다.")
    private String gender;

    public MemberSignupCommand toCommand() {
        return new MemberSignupCommand(
                email,
                pwd,
                nickName,
                gender
        );
    }

}
