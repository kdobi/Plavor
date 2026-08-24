package com.plavor.member.dto;

import com.plavor.member.domain.Member;
import com.plavor.member.domain.MemberRole;
import com.plavor.member.domain.MemberStatus;

public record MemberMeResponse(
		Long id,
		String email,
		String name,
		String phone,
		MemberRole role,
		MemberStatus status
) {

	public static MemberMeResponse from(Member member) {
		return new MemberMeResponse(
				member.getId(),
				member.getEmail(),
				member.getName(),
				member.getPhone(),
				member.getRole(),
				member.getStatus()
		);
	}
}
