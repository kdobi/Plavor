package com.plavor.catalog.service;

import com.plavor.catalog.domain.Product;
import com.plavor.catalog.domain.ProductStatus;
import com.plavor.catalog.dto.CategoryResponse;
import com.plavor.catalog.dto.ProductDetailResponse;
import com.plavor.catalog.dto.ProductSummaryResponse;
import com.plavor.catalog.repository.CategoryRepository;
import com.plavor.catalog.repository.ProductRepository;
import com.plavor.global.common.PageResponse;
import com.plavor.global.error.BusinessException;
import com.plavor.global.error.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CatalogService {

	private static final List<ProductStatus> PUBLIC_PRODUCT_STATUSES = List.of(
			ProductStatus.ACTIVE,
			ProductStatus.SOLD_OUT
	);

	private final ProductRepository productRepository;
	private final CategoryRepository categoryRepository;

	public CatalogService(ProductRepository productRepository, CategoryRepository categoryRepository) {
		this.productRepository = productRepository;
		this.categoryRepository = categoryRepository;
	}

	public PageResponse<ProductSummaryResponse> getProducts(Long categoryId, String keyword, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<Long> productIds = findPublicProductIds(categoryId, normalize(keyword), pageable);

		if (productIds.isEmpty()) {
			return PageResponse.from(new PageImpl<>(List.of(), pageable, productIds.getTotalElements()));
		}

		List<Product> products = productRepository.findAllPublicProductsWithDetails(
				productIds.getContent(),
				PUBLIC_PRODUCT_STATUSES
		);
		Map<Long, Product> productsById = products.stream()
				.collect(Collectors.toMap(Product::getId, Function.identity()));

		List<ProductSummaryResponse> content = productIds.getContent().stream()
				.map(productsById::get)
				.filter(Objects::nonNull)
				.map(ProductSummaryResponse::from)
				.toList();

		return PageResponse.from(new PageImpl<>(content, pageable, productIds.getTotalElements()));
	}

	public ProductDetailResponse getProduct(Long productId) {
		Product product = productRepository.findPublicProductById(productId, PUBLIC_PRODUCT_STATUSES)
				.orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "상품을 찾을 수 없습니다."));

		return ProductDetailResponse.from(product);
	}

	public List<CategoryResponse> getCategories() {
		return categoryRepository.findByActiveTrueOrderByDisplayOrderAscIdAsc().stream()
				.map(CategoryResponse::from)
				.toList();
	}

	private String normalize(String keyword) {
		if (keyword == null || keyword.isBlank()) {
			return null;
		}

		return keyword.trim().toLowerCase();
	}

	private Page<Long> findPublicProductIds(Long categoryId, String keyword, Pageable pageable) {
		if (categoryId != null && keyword != null) {
			return productRepository.findPublicProductIdsByCategoryIdAndKeyword(
					PUBLIC_PRODUCT_STATUSES,
					categoryId,
					keyword,
					pageable
			);
		}

		if (categoryId != null) {
			return productRepository.findPublicProductIdsByCategoryId(PUBLIC_PRODUCT_STATUSES, categoryId, pageable);
		}

		if (keyword != null) {
			return productRepository.findPublicProductIdsByKeyword(PUBLIC_PRODUCT_STATUSES, keyword, pageable);
		}

		return productRepository.findPublicProductIds(PUBLIC_PRODUCT_STATUSES, pageable);
	}
}
