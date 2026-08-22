package com.plavor.catalog.repository;

import com.plavor.catalog.domain.Product;
import com.plavor.catalog.domain.ProductStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductRepositoryTests {

	@Autowired
	private ProductRepository productRepository;

	@Test
	void findByIdLoadsProductWithCategoryAndImages() {
		Product product = productRepository.findById(1L).orElseThrow();

		assertThat(product.getName()).isEqualTo("Minimal Cotton T-Shirt");
		assertThat(product.getPrice()).isEqualTo(29000);
		assertThat(product.getStockQuantity()).isEqualTo(120);
		assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);

		assertThat(product.getCategory().getName()).isEqualTo("Tops");
		assertThat(product.getCategory().getSlug()).isEqualTo("tops");
		assertThat(product.getCategory().isActive()).isTrue();

		assertThat(product.getImages()).hasSize(1);
		assertThat(product.getImages().getFirst().getImageUrl())
				.isEqualTo("https://images.unsplash.com/photo-1521572163474-6864f9cf17ab");
		assertThat(product.getImages().getFirst().isThumbnail()).isTrue();
	}

	@Test
	void findByIdLoadsHiddenProductStatus() {
		Product product = productRepository.findById(4L).orElseThrow();

		assertThat(product.getStatus()).isEqualTo(ProductStatus.HIDDEN);
	}
}
