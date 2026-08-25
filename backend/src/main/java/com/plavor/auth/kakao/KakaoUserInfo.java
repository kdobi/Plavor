package com.plavor.auth.kakao;

public record KakaoUserInfo(
		String providerUserId,
		String email,
		String nickname
) {
}
