package com.plavor.auth.service;

import com.plavor.auth.dto.AuthUserResponse;
import com.plavor.auth.dto.AuthTokenResponse;
import com.plavor.auth.dto.LoginRequest;
import com.plavor.auth.dto.SignupRequest;
import com.plavor.global.error.BusinessException;
import com.plavor.global.error.ErrorCode;
import com.plavor.global.security.JwtToken;
import com.plavor.global.security.JwtTokenProvider;
import com.plavor.member.domain.Member;
import com.plavor.member.domain.MemberRole;
import com.plavor.member.domain.UserCredential;
import com.plavor.member.repository.MemberRepository;
import com.plavor.member.repository.UserCredentialRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

	private final MemberRepository memberRepository;
	private final UserCredentialRepository userCredentialRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;

	public AuthService(
			MemberRepository memberRepository,
			UserCredentialRepository userCredentialRepository,
			PasswordEncoder passwordEncoder,
			JwtTokenProvider jwtTokenProvider
	) {
		this.memberRepository = memberRepository;
		this.userCredentialRepository = userCredentialRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenProvider = jwtTokenProvider;
	}

	public AuthUserResponse signup(SignupRequest request) {
		String email = request.email().trim().toLowerCase();

		if (memberRepository.existsByEmail(email)) {
			throw new BusinessException(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS);
		}

		Member member = memberRepository.save(new Member(
				email,
				request.name().trim(),
				normalizePhone(request.phone()),
				MemberRole.USER
		));
		userCredentialRepository.save(new UserCredential(
				member,
				passwordEncoder.encode(request.password()),
				false
		));

		return AuthUserResponse.from(member);
	}

	@Transactional(readOnly = true)
	public AuthTokenResponse login(LoginRequest request) {
		Member member = memberRepository.findByEmail(normalizeEmail(request.email()))
				.orElseThrow(this::invalidCredentials);
		UserCredential credential = userCredentialRepository.findByMemberId(member.getId())
				.orElseThrow(this::invalidCredentials);

		if (!passwordEncoder.matches(request.password(), credential.getPasswordHash())) {
			throw invalidCredentials();
		}

		JwtToken accessToken = jwtTokenProvider.generateAccessToken(member);
		return AuthTokenResponse.of(accessToken, member);
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase();
	}

	private String normalizePhone(String phone) {
		if (phone == null || phone.isBlank()) {
			return null;
		}

		return phone.trim();
	}

	private BusinessException invalidCredentials() {
		return new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS);
	}
}
