package com.plavor.member.repository;

import com.plavor.member.domain.SocialAccount;
import com.plavor.member.domain.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

	Optional<SocialAccount> findByProviderAndProviderUserId(
			SocialProvider provider,
			String providerUserId
	);

	boolean existsByProviderAndProviderUserId(SocialProvider provider, String providerUserId);
}
