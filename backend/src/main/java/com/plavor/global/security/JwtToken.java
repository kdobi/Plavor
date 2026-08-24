package com.plavor.global.security;

public record JwtToken(
		String value,
		long expiresInSeconds
) {
}
