package kr.mclub.apiserver.user.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 회원가입 요청 DTO / Sign up request
 *
 * @param email 이메일
 * @param password 비밀번호 (8~20자, 영문+숫자+특수문자 조합)
 * @param realName 실명
 * @param phoneNumber 전화번호
 */
public record SignUpRequest(
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "이메일 형식이 올바르지 않습니다")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다")
        @Size(min = 8, max = 20, message = "비밀번호는 8~20자여야 합니다")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]+$",
                message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다"
        )
        String password,

        @NotBlank(message = "실명은 필수입니다")
        @Size(min = 2, max = 50, message = "실명은 2~50자여야 합니다")
        String realName,

        @Pattern(
                regexp = "^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$",
                message = "전화번호 형식이 올바르지 않습니다"
        )
        String phoneNumber
) {}
