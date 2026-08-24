package com.plavor.member.controller;

import com.plavor.global.security.AuthenticatedMember;
import com.plavor.member.dto.MemberMeResponse;
import com.plavor.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Member", description = "회원 API")
@RestController
@RequestMapping("/api/members")
public class MemberController {

	private final MemberService memberService;

	public MemberController(MemberService memberService) {
		this.memberService = memberService;
	}

	@Operation(summary = "내 정보 조회", description = "JWT 액세스 토큰으로 로그인한 회원의 정보를 조회합니다.")
	@GetMapping("/me")
	public MemberMeResponse getMe(@AuthenticationPrincipal AuthenticatedMember authenticatedMember) {
		return memberService.getMe(authenticatedMember.id());
	}
}
