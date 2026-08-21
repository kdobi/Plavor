package com.plavor.backend.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

	@Query("""
			select distinct p
			from Product p
			join fetch p.category
			left join fetch p.images
			where p.status in :statuses
			order by p.createdAt desc, p.id desc
			""")
	List<Product> findCatalogProducts(@Param("statuses") Collection<ProductStatus> statuses);

	@Query("""
			select distinct p
			from Product p
			join fetch p.category
			left join fetch p.images
			where p.id = :id
			  and p.status in :statuses
			""")
	Optional<Product> findCatalogProductById(
			@Param("id") Long id,
			@Param("statuses") Collection<ProductStatus> statuses
	);
}
