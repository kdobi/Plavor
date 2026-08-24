package com.plavor.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
		@NotBlank(message = "이메일은 필수입니다.")
		@Email(message = "이메일 형식이 올바르지 않습니다.")
		@Size(max = 255, message = "이메일은 255자 이하여야 합니다.")
		String email,

		@NotBlank(message = "비밀번호는 필수입니다.")
		@Size(min = 8, max = 72, message = "비밀번호는 8자 이상 72자 이하여야 합니다.")
		String password,

		@NotBlank(message = "이름은 필수입니다.")
		@Size(max = 100, message = "이름은 100자 이하여야 합니다.")
		String name,

		@Size(max = 30, message = "전화번호는 30자 이하여야 합니다.")
		String phone
) {
}
