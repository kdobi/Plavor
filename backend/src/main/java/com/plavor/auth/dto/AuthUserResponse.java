package com.plavor.auth.dto;

import com.plavor.member.domain.Member;
import com.plavor.member.domain.MemberRole;
import com.plavor.member.domain.MemberStatus;

public record AuthUserResponse(
		Long id,
		String email,
		String name,
		String phone,
		MemberRole role,
		MemberStatus status
) {

	public static AuthUserResponse from(Member member) {
		return new AuthUserResponse(
				member.getId(),
				member.getEmail(),
				member.getName(),
				member.getPhone(),
				member.getRole(),
				member.getStatus()
		);
	}
}
