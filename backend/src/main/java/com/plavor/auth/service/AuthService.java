package com.plavor.auth.service;

import com.plavor.auth.dto.AuthTokenResponse;
import com.plavor.auth.dto.AuthUserResponse;
import com.plavor.auth.dto.KakaoLoginRequest;
import com.plavor.auth.dto.KakaoLoginUrlResponse;
import com.plavor.auth.dto.LoginRequest;
import com.plavor.auth.dto.SignupRequest;
import com.plavor.auth.kakao.KakaoClient;
import com.plavor.auth.kakao.KakaoUserInfo;
import com.plavor.global.error.BusinessException;
import com.plavor.global.error.ErrorCode;
import com.plavor.global.security.JwtToken;
import com.plavor.global.security.JwtTokenProvider;
import com.plavor.member.domain.Member;
import com.plavor.member.domain.MemberRole;
import com.plavor.member.domain.SocialAccount;
import com.plavor.member.domain.SocialProvider;
import com.plavor.member.domain.UserCredential;
import com.plavor.member.repository.MemberRepository;
import com.plavor.member.repository.SocialAccountRepository;
import com.plavor.member.repository.UserCredentialRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

	private final MemberRepository memberRepository;
	private final UserCredentialRepository userCredentialRepository;
	private final SocialAccountRepository socialAccountRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final KakaoClient kakaoClient;

	public AuthService(
			MemberRepository memberRepository,
			UserCredentialRepository userCredentialRepository,
			SocialAccountRepository socialAccountRepository,
			PasswordEncoder passwordEncoder,
			JwtTokenProvider jwtTokenProvider,
			KakaoClient kakaoClient
	) {
		this.memberRepository = memberRepository;
		this.userCredentialRepository = userCredentialRepository;
		this.socialAccountRepository = socialAccountRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenProvider = jwtTokenProvider;
		this.kakaoClient = kakaoClient;
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

	@Transactional(readOnly = true)
	public KakaoLoginUrlResponse getKakaoLoginUrl(String state) {
		return new KakaoLoginUrlResponse(kakaoClient.createAuthorizationUrl(state));
	}

	public AuthTokenResponse loginWithKakao(KakaoLoginRequest request) {
		KakaoUserInfo kakaoUser = kakaoClient.fetchUser(request.code());
		Member member = socialAccountRepository
				.findByProviderAndProviderUserId(SocialProvider.KAKAO, kakaoUser.providerUserId())
				.map(SocialAccount::getMember)
				.orElseGet(() -> createKakaoMember(kakaoUser));

		JwtToken accessToken = jwtTokenProvider.generateAccessToken(member);
		return AuthTokenResponse.of(accessToken, member);
	}

	private Member createKakaoMember(KakaoUserInfo kakaoUser) {
		String internalEmail = createKakaoInternalEmail(kakaoUser.providerUserId());
		Member member = memberRepository.findByEmail(internalEmail)
				.orElseGet(() -> memberRepository.save(new Member(
						internalEmail,
						normalizeKakaoNickname(kakaoUser.nickname()),
						null,
						MemberRole.USER
				)));

		socialAccountRepository.save(new SocialAccount(
				member,
				SocialProvider.KAKAO,
				kakaoUser.providerUserId(),
				kakaoUser.email()
		));

		return member;
	}

	private String createKakaoInternalEmail(String providerUserId) {
		return "kakao_" + providerUserId + "@social.plavor.local";
	}

	private String normalizeKakaoNickname(String nickname) {
		if (nickname == null || nickname.isBlank()) {
			return "카카오 사용자";
		}

		String trimmed = nickname.trim();
		if (trimmed.length() > 100) {
			return trimmed.substring(0, 100);
		}

		return trimmed;
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
