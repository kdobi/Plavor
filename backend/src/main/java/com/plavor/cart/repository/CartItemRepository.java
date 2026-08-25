package com.plavor.cart.repository;

import com.plavor.cart.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

	@Query("""
			select ci
			from CartItem ci
			join fetch ci.cart c
			join fetch ci.product p
			where ci.id = :itemId
			and c.member.id = :memberId
			""")
	Optional<CartItem> findByIdAndMemberId(
			@Param("itemId") Long itemId,
			@Param("memberId") Long memberId
	);
}
