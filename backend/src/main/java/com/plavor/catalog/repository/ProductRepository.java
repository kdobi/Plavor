package com.plavor.catalog.repository;

import com.plavor.catalog.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
