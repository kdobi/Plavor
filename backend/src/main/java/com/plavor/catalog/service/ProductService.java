package com.plavor.catalog.service;

import com.plavor.catalog.domain.Product;
import com.plavor.catalog.domain.ProductStatus;
import com.plavor.catalog.dto.ProductDetailResponse;
import com.plavor.catalog.dto.ProductSummaryResponse;
import com.plavor.catalog.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductService {

	private static final List<ProductStatus> PUBLIC_STATUSES = List.of(
			ProductStatus.ACTIVE,
			ProductStatus.SOLD_OUT
	);

	private final ProductRepository productRepository;

	public ProductService(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	public List<ProductSummaryResponse> getProducts() {
		return productRepository.findCatalogProducts(PUBLIC_STATUSES).stream()
				.map(ProductSummaryResponse::from)
				.toList();
	}

	public ProductDetailResponse getProduct(Long id) {
		Product product = productRepository.findCatalogProductById(id, PUBLIC_STATUSES)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

		return ProductDetailResponse.from(product);
	}
}
