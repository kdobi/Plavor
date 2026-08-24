package com.plavor.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtTokenProvider jwtTokenProvider;
	private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;

	public JwtAuthenticationFilter(
			JwtTokenProvider jwtTokenProvider,
			RestAuthenticationEntryPoint restAuthenticationEntryPoint
	) {
		this.jwtTokenProvider = jwtTokenProvider;
		this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		String accessToken = resolveAccessToken(request);

		if (accessToken == null) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			AuthenticatedMember authenticatedMember = jwtTokenProvider.parseAccessToken(accessToken);
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
					authenticatedMember,
					null,
					authenticatedMember.authorities()
			);
			SecurityContextHolder.getContext().setAuthentication(authentication);
			filterChain.doFilter(request, response);
		} catch (JwtAuthenticationException exception) {
			SecurityContextHolder.clearContext();
			restAuthenticationEntryPoint.commence(request, response, exception);
		}
	}

	private String resolveAccessToken(HttpServletRequest request) {
		String authorization = request.getHeader(AUTHORIZATION_HEADER);
		if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
			return null;
		}

		String token = authorization.substring(BEARER_PREFIX.length()).trim();
		if (token.isBlank()) {
			return null;
		}

		return token;
	}
}
