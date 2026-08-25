package com.plavor.catalog.repository;

import com.plavor.catalog.domain.Product;
import com.plavor.catalog.domain.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

	@Query(
			value = """
					select p.id
					from Product p
					join p.category c
					where p.status in :statuses
					and c.active = true
					order by p.id desc
					""",
			countQuery = """
					select count(p)
					from Product p
					join p.category c
					where p.status in :statuses
					and c.active = true
					"""
	)
	Page<Long> findPublicProductIds(
			@Param("statuses") Collection<ProductStatus> statuses,
			Pageable pageable
	);

	@Query(
			value = """
					select p.id
					from Product p
					join p.category c
					where p.status in :statuses
					and c.active = true
					and c.id = :categoryId
					order by p.id desc
					""",
			countQuery = """
					select count(p)
					from Product p
					join p.category c
					where p.status in :statuses
					and c.active = true
					and c.id = :categoryId
					"""
	)
	Page<Long> findPublicProductIdsByCategoryId(
			@Param("statuses") Collection<ProductStatus> statuses,
			@Param("categoryId") Long categoryId,
			Pageable pageable
	);

	@Query(
			value = """
					select p.id
					from Product p
					join p.category c
					where p.status in :statuses
					and c.active = true
					and lower(p.name) like concat('%', :keyword, '%')
					order by p.id desc
					""",
			countQuery = """
					select count(p)
					from Product p
					join p.category c
					where p.status in :statuses
					and c.active = true
					and lower(p.name) like concat('%', :keyword, '%')
					"""
	)
	Page<Long> findPublicProductIdsByKeyword(
			@Param("statuses") Collection<ProductStatus> statuses,
			@Param("keyword") String keyword,
			Pageable pageable
	);

	@Query(
			value = """
					select p.id
					from Product p
					join p.category c
					where p.status in :statuses
					and c.active = true
					and c.id = :categoryId
					and lower(p.name) like concat('%', :keyword, '%')
					order by p.id desc
					""",
			countQuery = """
					select count(p)
					from Product p
					join p.category c
					where p.status in :statuses
					and c.active = true
					and c.id = :categoryId
					and lower(p.name) like concat('%', :keyword, '%')
					"""
	)
	Page<Long> findPublicProductIdsByCategoryIdAndKeyword(
			@Param("statuses") Collection<ProductStatus> statuses,
			@Param("categoryId") Long categoryId,
			@Param("keyword") String keyword,
			Pageable pageable
	);

	@Query("""
			select distinct p
			from Product p
			join fetch p.category c
			left join fetch p.images
			where p.id in :ids
			and p.status in :statuses
			and c.active = true
			""")
	List<Product> findAllPublicProductsWithDetails(
			@Param("ids") Collection<Long> ids,
			@Param("statuses") Collection<ProductStatus> statuses
	);

	@Query("""
			select distinct p
			from Product p
			join fetch p.category c
			left join fetch p.images
			where p.id = :id
			and p.status in :statuses
			and c.active = true
			""")
	Optional<Product> findPublicProductById(
			@Param("id") Long id,
			@Param("statuses") Collection<ProductStatus> statuses
	);

	@Query(
			value = """
					select p.id
					from Product p
					join p.category c
					where (:categoryId is null or c.id = :categoryId)
					and (:status is null or p.status = :status)
					and lower(p.name) like concat('%', :keyword, '%')
					order by p.id desc
					""",
			countQuery = """
					select count(p)
					from Product p
					join p.category c
					where (:categoryId is null or c.id = :categoryId)
					and (:status is null or p.status = :status)
					and lower(p.name) like concat('%', :keyword, '%')
					"""
	)
	Page<Long> findAdminProductIds(
			@Param("categoryId") Long categoryId,
			@Param("status") ProductStatus status,
			@Param("keyword") String keyword,
			Pageable pageable
	);

	@Query("""
			select distinct p
			from Product p
			join fetch p.category
			left join fetch p.images
			where p.id in :ids
			""")
	List<Product> findAllAdminProductsWithDetails(@Param("ids") Collection<Long> ids);

	@Query("""
			select distinct p
			from Product p
			join fetch p.category
			left join fetch p.images
			where p.id = :id
			""")
	Optional<Product> findAdminProductByIdWithDetails(@Param("id") Long id);
}
