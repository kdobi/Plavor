package com.plavor.auth.service;

import com.plavor.auth.domain.RefreshToken;
import com.plavor.auth.repository.RefreshTokenRepository;
import com.plavor.global.error.BusinessException;
import com.plavor.global.error.ErrorCode;
import com.plavor.global.security.JwtProperties;
import com.plavor.member.domain.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@Transactional
public class RefreshTokenService {

	private static final int REFRESH_TOKEN_BYTE_LENGTH = 64;
	private static final char[] HEX = "0123456789abcdef".toCharArray();

	private final SecureRandom secureRandom = new SecureRandom();
	private final RefreshTokenRepository refreshTokenRepository;
	private final JwtProperties jwtProperties;

	public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, JwtProperties jwtProperties) {
		this.refreshTokenRepository = refreshTokenRepository;
		this.jwtProperties = jwtProperties;
	}

	public IssuedRefreshToken issue(Member member) {
		String value = generateTokenValue();
		long expiresInSeconds = jwtProperties.getRefreshTokenExpirationSeconds();
		LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expiresInSeconds);

		refreshTokenRepository.save(new RefreshToken(member, hash(value), expiresAt));

		return new IssuedRefreshToken(value, expiresInSeconds);
	}

	public Member verifyAndRevoke(String value) {
		RefreshToken refreshToken = findValidRefreshToken(value);
		refreshToken.revoke(LocalDateTime.now());

		return refreshToken.getMember();
	}

	public void revoke(String value) {
		if (value == null || value.isBlank()) {
			return;
		}

		refreshTokenRepository.findByTokenHash(hash(value))
				.filter(refreshToken -> refreshToken.isActive(LocalDateTime.now()))
				.ifPresent(refreshToken -> refreshToken.revoke(LocalDateTime.now()));
	}

	private RefreshToken findValidRefreshToken(String value) {
		if (value == null || value.isBlank()) {
			throw invalidRefreshToken();
		}

		RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hash(value))
				.orElseThrow(this::invalidRefreshToken);

		if (!refreshToken.isActive(LocalDateTime.now())) {
			throw invalidRefreshToken();
		}

		return refreshToken;
	}

	private String generateTokenValue() {
		byte[] bytes = new byte[REFRESH_TOKEN_BYTE_LENGTH];
		secureRandom.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private String hash(String value) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256")
					.digest(value.getBytes(StandardCharsets.UTF_8));
			return toHex(digest);
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 algorithm is not available.", exception);
		}
	}

	private String toHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int index = 0; index < bytes.length; index++) {
			int value = bytes[index] & 0xFF;
			hexChars[index * 2] = HEX[value >>> 4];
			hexChars[index * 2 + 1] = HEX[value & 0x0F];
		}
		return new String(hexChars);
	}

	private BusinessException invalidRefreshToken() {
		return new BusinessException(ErrorCode.AUTH_INVALID_REFRESH_TOKEN);
	}
}
