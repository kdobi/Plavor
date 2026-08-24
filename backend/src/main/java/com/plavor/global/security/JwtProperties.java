package com.plavor.global.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

	private String secret;
	private long accessTokenExpirationSeconds;

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public long getAccessTokenExpirationSeconds() {
		return accessTokenExpirationSeconds;
	}

	public void setAccessTokenExpirationSeconds(long accessTokenExpirationSeconds) {
		this.accessTokenExpirationSeconds = accessTokenExpirationSeconds;
	}
}
