package com.plavor.auth.service;

public record IssuedRefreshToken(
		String value,
		long expiresInSeconds
) {
}
