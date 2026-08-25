package com.plavor.admin.service;

import com.plavor.admin.dto.AdminProductImageRequest;
import com.plavor.admin.dto.AdminProductRequest;
import com.plavor.admin.dto.AdminProductResponse;
import com.plavor.admin.dto.AdminProductStatusUpdateRequest;
import com.plavor.catalog.domain.Category;
import com.plavor.catalog.domain.Product;
import com.plavor.catalog.domain.ProductImage;
import com.plavor.catalog.domain.ProductStatus;
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
@Transactional
public class AdminProductService {

	private final ProductRepository productRepository;
	private final CategoryRepository categoryRepository;

	public AdminProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
		this.productRepository = productRepository;
		this.categoryRepository = categoryRepository;
	}

	@Transactional(readOnly = true)
	public PageResponse<AdminProductResponse> getProducts(
			Long categoryId,
			ProductStatus status,
			String keyword,
			int page,
			int size
	) {
		Pageable pageable = PageRequest.of(page, size);
		Page<Long> productIds = productRepository.findAdminProductIds(
				categoryId,
				status,
				normalizeKeyword(keyword),
				pageable
		);

		if (productIds.isEmpty()) {
			return PageResponse.from(new PageImpl<>(List.of(), pageable, productIds.getTotalElements()));
		}

		List<Product> products = productRepository.findAllAdminProductsWithDetails(productIds.getContent());
		Map<Long, Product> productsById = products.stream()
				.collect(Collectors.toMap(Product::getId, Function.identity()));
		List<AdminProductResponse> content = productIds.getContent().stream()
				.map(productsById::get)
				.filter(Objects::nonNull)
				.map(AdminProductResponse::from)
				.toList();

		return PageResponse.from(new PageImpl<>(content, pageable, productIds.getTotalElements()));
	}

	@Transactional(readOnly = true)
	public AdminProductResponse getProduct(Long productId) {
		return AdminProductResponse.from(getProductWithDetails(productId));
	}

	public AdminProductResponse createProduct(AdminProductRequest request) {
		Category category = getCategory(request.categoryId());
		validateProductPolicy(request);

		Product product = new Product(
				category,
				normalizeRequired(request.name()),
				normalizeNullable(request.description()),
				request.price(),
				request.stockQuantity(),
				request.status()
		);
		product.replaceImages(createImages(product, request.images()));

		Product savedProduct = productRepository.save(product);
		productRepository.flush();

		return AdminProductResponse.from(savedProduct);
	}

	public AdminProductResponse updateProduct(Long productId, AdminProductRequest request) {
		Product product = getProductWithDetails(productId);
		Category category = getCategory(request.categoryId());
		validateProductPolicy(request);

		product.update(
				category,
				normalizeRequired(request.name()),
				normalizeNullable(request.description()),
				request.price(),
				request.stockQuantity(),
				request.status()
		);
		product.replaceImages(createImages(product, request.images()));
		productRepository.flush();

		return AdminProductResponse.from(product);
	}

	public AdminProductResponse updateStatus(Long productId, AdminProductStatusUpdateRequest request) {
		Product product = getProductWithDetails(productId);

		if (request.status() == ProductStatus.ACTIVE && product.getStockQuantity() == 0) {
			throw new BusinessException(ErrorCode.COMMON_INVALID_INPUT, "재고가 없는 상품은 판매중으로 변경할 수 없습니다.");
		}

		product.update(
				product.getCategory(),
				product.getName(),
				product.getDescription(),
				product.getPrice(),
				product.getStockQuantity(),
				request.status()
		);
		productRepository.flush();

		return AdminProductResponse.from(product);
	}

	private Product getProductWithDetails(Long productId) {
		return productRepository.findAdminProductByIdWithDetails(productId)
				.orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "상품을 찾을 수 없습니다."));
	}

	private Category getCategory(Long categoryId) {
		return categoryRepository.findById(categoryId)
				.orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "카테고리를 찾을 수 없습니다."));
	}

	private void validateProductPolicy(AdminProductRequest request) {
		if (request.status() == ProductStatus.ACTIVE && request.stockQuantity() == 0) {
			throw new BusinessException(ErrorCode.COMMON_INVALID_INPUT, "재고가 없는 상품은 판매중으로 등록할 수 없습니다.");
		}

		List<AdminProductImageRequest> images = normalizeImages(request.images());
		long thumbnailCount = images.stream()
				.filter(AdminProductImageRequest::thumbnail)
				.count();
		if (thumbnailCount > 1) {
			throw new BusinessException(ErrorCode.COMMON_INVALID_INPUT, "대표 이미지는 하나만 지정할 수 있습니다.");
		}
	}

	private List<ProductImage> createImages(Product product, List<AdminProductImageRequest> images) {
		List<AdminProductImageRequest> normalizedImages = normalizeImages(images);
		boolean hasThumbnail = normalizedImages.stream().anyMatch(AdminProductImageRequest::thumbnail);

		return normalizedImages.stream()
				.map(image -> new ProductImage(
						product,
						normalizeRequired(image.imageUrl()),
						normalizeNullable(image.altText()),
						image.displayOrder(),
						image.thumbnail() || (!hasThumbnail && image == normalizedImages.get(0))
				))
				.toList();
	}

	private List<AdminProductImageRequest> normalizeImages(List<AdminProductImageRequest> images) {
		if (images == null) {
			return List.of();
		}

		return images;
	}

	private String normalize(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}

		return value.trim().toLowerCase();
	}

	private String normalizeKeyword(String keyword) {
		String normalizedKeyword = normalize(keyword);
		if (normalizedKeyword == null) {
			return "";
		}

		return normalizedKeyword;
	}

	private String normalizeRequired(String value) {
		return value.trim();
	}

	private String normalizeNullable(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}

		return value.trim();
	}
}
