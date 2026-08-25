package com.plavor.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record KakaoLoginRequest(
		@NotBlank(message = "카카오 인증 코드는 필수입니다.")
		String code,

		@NotBlank(message = "카카오 인증 상태 값은 필수입니다.")
		String state
) {
}
