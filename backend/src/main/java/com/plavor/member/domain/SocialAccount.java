package com.plavor.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
		name = "social_accounts",
		uniqueConstraints = {
				@UniqueConstraint(
						name = "uk_social_accounts_provider_user",
						columnNames = {"provider", "provider_user_id"}
				),
				@UniqueConstraint(
						name = "uk_social_accounts_user_provider",
						columnNames = {"user_id", "provider"}
				)
		},
		indexes = @Index(name = "idx_social_accounts_user_id", columnList = "user_id")
)
public class SocialAccount {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private Member member;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private SocialProvider provider;

	@Column(name = "provider_user_id", nullable = false, length = 100)
	private String providerUserId;

	@Column(name = "provider_email", length = 255)
	private String providerEmail;

	@CreationTimestamp
	@Column(name = "connected_at", nullable = false, updatable = false)
	private LocalDateTime connectedAt;

	protected SocialAccount() {
	}

	public SocialAccount(
			Member member,
			SocialProvider provider,
			String providerUserId,
			String providerEmail
	) {
		this.member = member;
		this.provider = provider;
		this.providerUserId = providerUserId;
		this.providerEmail = providerEmail;
	}

	public Long getId() {
		return id;
	}

	public Member getMember() {
		return member;
	}

	public SocialProvider getProvider() {
		return provider;
	}

	public String getProviderUserId() {
		return providerUserId;
	}

	public String getProviderEmail() {
		return providerEmail;
	}

	public LocalDateTime getConnectedAt() {
		return connectedAt;
	}
}
