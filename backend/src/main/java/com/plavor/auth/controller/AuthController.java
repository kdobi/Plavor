package com.plavor.auth.controller;

import com.plavor.auth.dto.AuthTokenResponse;
import com.plavor.auth.dto.AuthUserResponse;
import com.plavor.auth.dto.KakaoLoginRequest;
import com.plavor.auth.dto.KakaoLoginUrlResponse;
import com.plavor.auth.dto.LoginRequest;
import com.plavor.auth.dto.SignupRequest;
import com.plavor.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "회원가입 및 로그인 API")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@Operation(summary = "이메일 회원가입", description = "이메일과 비밀번호로 Plavor 회원을 생성합니다.")
	@PostMapping("/signup")
	@ResponseStatus(HttpStatus.CREATED)
	public AuthUserResponse signup(@Valid @RequestBody SignupRequest request) {
		return authService.signup(request);
	}

	@Operation(summary = "이메일 로그인", description = "이메일과 비밀번호를 검증하고 JWT 액세스 토큰을 발급합니다.")
	@PostMapping("/login")
	public AuthTokenResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

	@Operation(summary = "카카오 로그인 URL 조회", description = "프론트에서 이동할 카카오 OAuth 인증 URL을 생성합니다.")
	@GetMapping("/kakao/login-url")
	public KakaoLoginUrlResponse getKakaoLoginUrl(
			@RequestParam(required = false) String state
	) {
		return authService.getKakaoLoginUrl(state);
	}

	@Operation(summary = "카카오 로그인", description = "카카오 OAuth 인증 코드를 검증하고 JWT 액세스 토큰을 발급합니다.")
	@PostMapping("/kakao/login")
	public AuthTokenResponse loginWithKakao(@Valid @RequestBody KakaoLoginRequest request) {
		return authService.loginWithKakao(request);
	}
}
