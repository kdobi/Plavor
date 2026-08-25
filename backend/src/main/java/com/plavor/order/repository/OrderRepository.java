package com.plavor.order.repository;

import com.plavor.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

	@Query("""
			select distinct o
			from Order o
			left join fetch o.items i
			left join fetch i.product
			where o.member.id = :memberId
			order by o.orderedAt desc, o.id desc
			""")
	List<Order> findAllByMemberIdWithItems(@Param("memberId") Long memberId);

	@Query("""
			select distinct o
			from Order o
			left join fetch o.items i
			left join fetch i.product
			where o.id = :orderId
			and o.member.id = :memberId
			""")
	Optional<Order> findByIdAndMemberIdWithItems(
			@Param("orderId") Long orderId,
			@Param("memberId") Long memberId
	);
}
