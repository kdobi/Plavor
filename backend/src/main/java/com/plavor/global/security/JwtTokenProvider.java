package com.plavor.global.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.plavor.global.error.ErrorCode;
import com.plavor.member.domain.Member;
import com.plavor.member.domain.MemberRole;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Component
public class JwtTokenProvider {

	private static final String HMAC_SHA256 = "HmacSHA256";
	private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();

	private final JwtProperties jwtProperties;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public JwtTokenProvider(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
	}

	public JwtToken generateAccessToken(Member member) {
		long now = Instant.now().getEpochSecond();
		long expiresInSeconds = jwtProperties.getAccessTokenExpirationSeconds();
		long expiresAt = now + expiresInSeconds;

		String header = encodeJson(Map.of(
				"alg", "HS256",
				"typ", "JWT"
		));
		String payload = encodeJson(Map.of(
				"sub", member.getId().toString(),
				"email", member.getEmail(),
				"role", member.getRole().name(),
				"iat", now,
				"exp", expiresAt
		));
		String unsignedToken = header + "." + payload;
		String signature = sign(unsignedToken);

		return new JwtToken(unsignedToken + "." + signature, expiresInSeconds);
	}

	public AuthenticatedMember parseAccessToken(String token) {
		String[] parts = token.split("\\.");
		if (parts.length != 3) {
			throw invalidToken();
		}

		String unsignedToken = parts[0] + "." + parts[1];
		if (!MessageDigest.isEqual(sign(unsignedToken).getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
			throw invalidToken();
		}

		Map<String, Object> payload = decodePayload(parts[1]);
		long expiresAt = readLong(payload, "exp");
		if (expiresAt < Instant.now().getEpochSecond()) {
			throw invalidToken();
		}

		try {
			return new AuthenticatedMember(
					Long.valueOf(readString(payload, "sub")),
					readString(payload, "email"),
					MemberRole.valueOf(readString(payload, "role"))
			);
		} catch (RuntimeException exception) {
			throw invalidToken();
		}
	}

	private String encodeJson(Map<String, Object> value) {
		try {
			return BASE64_URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("JWT payload serialization failed.", exception);
		}
	}

	private String sign(String unsignedToken) {
		String secret = jwtProperties.getSecret();
		if (secret == null || secret.isBlank()) {
			throw new IllegalStateException("JWT secret is not configured.");
		}

		try {
			Mac mac = Mac.getInstance(HMAC_SHA256);
			SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
			mac.init(secretKey);
			return BASE64_URL_ENCODER.encodeToString(mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8)));
		} catch (Exception exception) {
			throw new IllegalStateException("JWT signing failed.", exception);
		}
	}

	private Map<String, Object> decodePayload(String payload) {
		try {
			byte[] decodedPayload = Base64.getUrlDecoder().decode(payload);
			return objectMapper.readValue(decodedPayload, new TypeReference<>() {
			});
		} catch (Exception exception) {
			throw invalidToken();
		}
	}

	private String readString(Map<String, Object> payload, String key) {
		Object value = payload.get(key);
		if (!(value instanceof String stringValue) || stringValue.isBlank()) {
			throw invalidToken();
		}

		return stringValue;
	}

	private long readLong(Map<String, Object> payload, String key) {
		Object value = payload.get(key);
		if (!(value instanceof Number numberValue)) {
			throw invalidToken();
		}

		return numberValue.longValue();
	}

	private JwtAuthenticationException invalidToken() {
		return new JwtAuthenticationException(ErrorCode.AUTH_INVALID_TOKEN);
	}
}
