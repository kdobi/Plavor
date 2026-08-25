package com.plavor.auth.service;

import com.plavor.auth.dto.AuthTokenResponse;

public record AuthSession(
		AuthTokenResponse tokenResponse,
		String refreshToken,
		long refreshTokenExpiresInSeconds
) {
}
