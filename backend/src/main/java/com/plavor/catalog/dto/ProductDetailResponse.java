package com.plavor.catalog.dto;

import com.plavor.catalog.domain.Product;
import com.plavor.catalog.domain.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "상품 상세 응답")
public record ProductDetailResponse(
		@Schema(description = "상품 ID", example = "1")
		Long id,

		@Schema(description = "카테고리")
		CategoryResponse category,

		@Schema(description = "상품명", example = "Minimal Cotton T-Shirt")
		String name,

		@Schema(description = "상품 설명", example = "A clean everyday cotton T-shirt with a relaxed silhouette.")
		String description,

		@Schema(description = "상품 가격", example = "29000")
		long price,

		@Schema(description = "재고 수량", example = "120")
		int stockQuantity,

		@Schema(description = "상품 판매 상태", example = "ACTIVE")
		ProductStatus status,

		@Schema(description = "상품 이미지 목록")
		List<ProductImageResponse> images
) {

	public static ProductDetailResponse from(Product product) {
		return new ProductDetailResponse(
				product.getId(),
				CategoryResponse.from(product.getCategory()),
				product.getName(),
				product.getDescription(),
				product.getPrice(),
				product.getStockQuantity(),
				product.getStatus(),
				product.getImages().stream()
						.map(ProductImageResponse::from)
						.toList()
		);
	}
}
