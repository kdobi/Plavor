package com.plavor.member.service;

import com.plavor.global.error.BusinessException;
import com.plavor.global.error.ErrorCode;
import com.plavor.member.domain.Member;
import com.plavor.member.dto.MemberMeResponse;
import com.plavor.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberService {

	private final MemberRepository memberRepository;

	public MemberService(MemberRepository memberRepository) {
		this.memberRepository = memberRepository;
	}

	public MemberMeResponse getMe(Long memberId) {
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "회원을 찾을 수 없습니다."));

		return MemberMeResponse.from(member);
	}
}
