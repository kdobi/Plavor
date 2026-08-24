package com.plavor.global.security;

import com.plavor.member.domain.MemberRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public record AuthenticatedMember(
		Long id,
		String email,
		MemberRole role
) {

	public List<GrantedAuthority> authorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
	}
}
