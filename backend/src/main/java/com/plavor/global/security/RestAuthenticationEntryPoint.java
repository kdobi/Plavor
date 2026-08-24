package com.plavor.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plavor.global.error.ErrorCode;
import com.plavor.global.error.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void commence(
			HttpServletRequest request,
			HttpServletResponse response,
			AuthenticationException authException
	) throws IOException {
		ErrorCode errorCode = ErrorCode.AUTH_UNAUTHORIZED;
		if (authException instanceof JwtAuthenticationException jwtAuthenticationException) {
			errorCode = jwtAuthenticationException.getErrorCode();
		}

		response.setStatus(errorCode.getStatus().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		objectMapper.writeValue(response.getWriter(), ErrorResponse.of(errorCode));
	}
}
