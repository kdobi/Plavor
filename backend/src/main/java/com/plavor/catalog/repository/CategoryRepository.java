package com.plavor.catalog.repository;

import com.plavor.catalog.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

	List<Category> findByActiveTrueOrderByDisplayOrderAscIdAsc();
}
