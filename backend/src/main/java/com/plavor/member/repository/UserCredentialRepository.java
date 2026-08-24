package com.plavor.member.repository;

import com.plavor.member.domain.Member;
import com.plavor.member.domain.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCredentialRepository extends JpaRepository<UserCredential, Long> {

	Optional<UserCredential> findByMember(Member member);

	Optional<UserCredential> findByMemberId(Long memberId);
}
