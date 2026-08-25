package com.plavor.auth.controller;

import com.plavor.auth.dto.AuthTokenResponse;
import com.plavor.auth.dto.AuthUserResponse;
import com.plavor.auth.dto.KakaoLoginRequest;
import com.plavor.auth.dto.KakaoLoginUrlResponse;
import com.plavor.auth.dto.LoginRequest;
import com.plavor.auth.dto.SignupRequest;
import com.plavor.auth.kakao.KakaoProperties;
import com.plavor.auth.service.AuthSession;
import com.plavor.auth.service.AuthService;
import com.plavor.global.security.JwtProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@Tag(name = "Auth", description = "회원가입 및 로그인 API")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private static final String KAKAO_OAUTH_STATE_COOKIE_NAME = "PLAVOR_KAKAO_OAUTH_STATE";
	private static final String REFRESH_TOKEN_COOKIE_NAME = "PLAVOR_REFRESH_TOKEN";
	private static final String AUTH_COOKIE_PATH = "/api/auth";
	private static final Duration KAKAO_OAUTH_STATE_COOKIE_MAX_AGE = Duration.ofMinutes(5);

	private final AuthService authService;
	private final KakaoProperties kakaoProperties;
	private final JwtProperties jwtProperties;

	public AuthController(AuthService authService, KakaoProperties kakaoProperties, JwtProperties jwtProperties) {
		this.authService = authService;
		this.kakaoProperties = kakaoProperties;
		this.jwtProperties = jwtProperties;
	}

	@Operation(summary = "이메일 회원가입", description = "이메일과 비밀번호로 Plavor 회원을 생성합니다.")
	@PostMapping("/signup")
	@ResponseStatus(HttpStatus.CREATED)
	public AuthUserResponse signup(@Valid @RequestBody SignupRequest request) {
		return authService.signup(request);
	}

	@Operation(summary = "이메일 로그인", description = "이메일과 비밀번호를 검증하고 JWT 액세스 토큰을 발급합니다.")
	@PostMapping("/login")
	public ResponseEntity<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request) {
		AuthSession session = authService.login(request);

		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, createRefreshCookie(session).toString())
				.body(session.tokenResponse());
	}

	@Operation(summary = "카카오 로그인 URL 조회", description = "프론트에서 이동할 카카오 OAuth 인증 URL을 생성합니다.")
	@GetMapping("/kakao/login-url")
	public ResponseEntity<KakaoLoginUrlResponse> getKakaoLoginUrl() {
		String state = authService.createKakaoOAuthState();

		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, createStateCookie(state).toString())
				.body(authService.getKakaoLoginUrl(state));
	}

	@Operation(summary = "카카오 로그인", description = "카카오 OAuth 인증 코드를 검증하고 JWT 액세스 토큰을 발급합니다.")
	@PostMapping("/kakao/login")
	public ResponseEntity<AuthTokenResponse> loginWithKakao(
			@Valid @RequestBody KakaoLoginRequest request,
			@CookieValue(name = KAKAO_OAUTH_STATE_COOKIE_NAME, required = false) String storedState
	) {
		AuthSession session = authService.loginWithKakao(request, storedState);

		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, expireStateCookie().toString())
				.header(HttpHeaders.SET_COOKIE, createRefreshCookie(session).toString())
				.body(session.tokenResponse());
	}

	@Operation(summary = "액세스 토큰 재발급", description = "리프레시 토큰 쿠키를 검증하고 새 JWT 액세스 토큰을 발급합니다.")
	@PostMapping("/refresh")
	public ResponseEntity<AuthTokenResponse> refresh(
			@CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken
	) {
		AuthSession session = authService.refresh(refreshToken);

		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, createRefreshCookie(session).toString())
				.body(session.tokenResponse());
	}

	@Operation(summary = "로그아웃", description = "리프레시 토큰을 폐기하고 로그인 유지 쿠키를 삭제합니다.")
	@PostMapping("/logout")
	public ResponseEntity<Void> logout(
			@CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken
	) {
		authService.logout(refreshToken);

		return ResponseEntity.noContent()
				.header(HttpHeaders.SET_COOKIE, expireRefreshCookie().toString())
				.build();
	}

	private ResponseCookie createStateCookie(String state) {
		return ResponseCookie.from(KAKAO_OAUTH_STATE_COOKIE_NAME, state)
				.httpOnly(true)
				.secure(kakaoProperties.isStateCookieSecure())
				.sameSite("Lax")
				.path("/")
				.maxAge(KAKAO_OAUTH_STATE_COOKIE_MAX_AGE)
				.build();
	}

	private ResponseCookie expireStateCookie() {
		return ResponseCookie.from(KAKAO_OAUTH_STATE_COOKIE_NAME, "")
				.httpOnly(true)
				.secure(kakaoProperties.isStateCookieSecure())
				.sameSite("Lax")
				.path("/")
				.maxAge(Duration.ZERO)
				.build();
	}

	private ResponseCookie createRefreshCookie(AuthSession session) {
		return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, session.refreshToken())
				.httpOnly(true)
				.secure(jwtProperties.isRefreshCookieSecure())
				.sameSite("Lax")
				.path(AUTH_COOKIE_PATH)
				.maxAge(Duration.ofSeconds(session.refreshTokenExpiresInSeconds()))
				.build();
	}

	private ResponseCookie expireRefreshCookie() {
		return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
				.httpOnly(true)
				.secure(jwtProperties.isRefreshCookieSecure())
				.sameSite("Lax")
				.path(AUTH_COOKIE_PATH)
				.maxAge(Duration.ZERO)
				.build();
	}
}
