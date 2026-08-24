package com.plavor.member.repository;

import com.plavor.member.domain.Member;
import com.plavor.member.domain.MemberRole;
import com.plavor.member.domain.MemberStatus;
import com.plavor.member.domain.SocialAccount;
import com.plavor.member.domain.SocialProvider;
import com.plavor.member.domain.UserCredential;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MemberRepositoryTests {

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private UserCredentialRepository userCredentialRepository;

	@Autowired
	private SocialAccountRepository socialAccountRepository;

	@Autowired
	private EntityManager entityManager;

	@Test
	void saveEmailMemberWithCredential() {
		Member member = memberRepository.save(new Member(
				"email-member@example.com",
				"Email Member",
				"01012345678",
				MemberRole.USER
		));
		userCredentialRepository.save(new UserCredential(member, "{bcrypt}hashed-password", false));

		entityManager.flush();
		entityManager.clear();

		Member foundMember = memberRepository.findByEmail("email-member@example.com").orElseThrow();
		UserCredential foundCredential = userCredentialRepository.findByMemberId(foundMember.getId()).orElseThrow();

		assertThat(foundMember.getStatus()).isEqualTo(MemberStatus.ACTIVE);
		assertThat(foundMember.getRole()).isEqualTo(MemberRole.USER);
		assertThat(foundCredential.getPasswordHash()).isEqualTo("{bcrypt}hashed-password");
		assertThat(foundCredential.isEmailVerified()).isFalse();
	}

	@Test
	void saveKakaoMemberWithSocialAccount() {
		Member member = memberRepository.save(new Member(
				"kakao-member@example.com",
				"Kakao Member",
				null,
				MemberRole.USER
		));
		socialAccountRepository.save(new SocialAccount(
				member,
				SocialProvider.KAKAO,
				"3948571029",
				"kakao-member@example.com"
		));

		entityManager.flush();
		entityManager.clear();

		SocialAccount foundSocialAccount = socialAccountRepository
				.findByProviderAndProviderUserId(SocialProvider.KAKAO, "3948571029")
				.orElseThrow();

		assertThat(foundSocialAccount.getProvider()).isEqualTo(SocialProvider.KAKAO);
		assertThat(foundSocialAccount.getProviderEmail()).isEqualTo("kakao-member@example.com");
		assertThat(foundSocialAccount.getMember().getEmail()).isEqualTo("kakao-member@example.com");
		assertThat(userCredentialRepository.findByMemberId(foundSocialAccount.getMember().getId())).isEmpty();
	}

	@Test
	void existsByEmailReturnsTrueForSavedMember() {
		memberRepository.save(new Member(
				"duplicate-check@example.com",
				"Duplicate Check",
				null,
				MemberRole.USER
		));

		assertThat(memberRepository.existsByEmail("duplicate-check@example.com")).isTrue();
		assertThat(memberRepository.existsByEmail("missing@example.com")).isFalse();
	}
}
