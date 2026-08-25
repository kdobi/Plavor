package com.plavor.auth.kakao;

public interface KakaoClient {

	String createAuthorizationUrl(String state);

	KakaoUserInfo fetchUser(String code);
}
