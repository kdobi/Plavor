package com.plavor.auth.dto;

import com.plavor.global.security.JwtToken;
import com.plavor.member.domain.Member;

public record AuthTokenResponse(
		String accessToken,
		String tokenType,
		long expiresIn,
		AuthUserResponse user
) {

	public static AuthTokenResponse of(JwtToken token, Member member) {
		return new AuthTokenResponse(
				token.value(),
				"Bearer",
				token.expiresInSeconds(),
				AuthUserResponse.from(member)
		);
	}
}
