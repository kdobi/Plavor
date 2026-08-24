package com.plavor.auth.service;

import com.plavor.auth.dto.AuthUserResponse;
import com.plavor.auth.dto.SignupRequest;
import com.plavor.global.error.BusinessException;
import com.plavor.global.error.ErrorCode;
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

	public AuthService(
			MemberRepository memberRepository,
			UserCredentialRepository userCredentialRepository,
			PasswordEncoder passwordEncoder
	) {
		this.memberRepository = memberRepository;
		this.userCredentialRepository = userCredentialRepository;
		this.passwordEncoder = passwordEncoder;
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

	private String normalizePhone(String phone) {
		if (phone == null || phone.isBlank()) {
			return null;
		}

		return phone.trim();
	}
}
