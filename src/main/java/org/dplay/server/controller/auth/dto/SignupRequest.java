package org.dplay.server.controller.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.dplay.server.domain.user.Platform;

public record SignupRequest(
        @NotBlank(message = "로그인 플랫폼 값은 필수입니다.")
        Platform platform,
        @NotBlank(message = "유저 닉네임은 필수입니다.")
        @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하여야 합니다.")
        @Pattern(
                regexp = "^[가-힣a-zA-Z0-9]",
                message = "한글, 영문, 숫자만 입력 가능합니다."
        )
        String nickname
) {
}
