package com.plavor.order.repository;

import com.plavor.order.domain.Order;
import com.plavor.order.domain.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
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

	@Query(
			value = """
					select o.id
					from Order o
					join o.member m
					where (:status is null or o.status = :status)
					and (
						:keyword = ''
						or lower(o.orderNumber) like concat('%', :keyword, '%')
						or lower(o.receiverName) like concat('%', :keyword, '%')
						or lower(o.receiverPhone) like concat('%', :keyword, '%')
						or lower(m.email) like concat('%', :keyword, '%')
						or lower(m.name) like concat('%', :keyword, '%')
					)
					order by o.orderedAt desc, o.id desc
					""",
			countQuery = """
					select count(o)
					from Order o
					join o.member m
					where (:status is null or o.status = :status)
					and (
						:keyword = ''
						or lower(o.orderNumber) like concat('%', :keyword, '%')
						or lower(o.receiverName) like concat('%', :keyword, '%')
						or lower(o.receiverPhone) like concat('%', :keyword, '%')
						or lower(m.email) like concat('%', :keyword, '%')
						or lower(m.name) like concat('%', :keyword, '%')
					)
					"""
	)
	Page<Long> findAdminOrderIds(
			@Param("status") OrderStatus status,
			@Param("keyword") String keyword,
			Pageable pageable
	);

	@Query("""
			select distinct o
			from Order o
			join fetch o.member
			left join fetch o.items i
			left join fetch i.product
			where o.id in :ids
			""")
	List<Order> findAllAdminOrdersWithDetails(@Param("ids") Collection<Long> ids);

	@Query("""
			select distinct o
			from Order o
			join fetch o.member
			left join fetch o.items i
			left join fetch i.product
			where o.id = :id
			""")
	Optional<Order> findAdminOrderByIdWithDetails(@Param("id") Long id);
}
