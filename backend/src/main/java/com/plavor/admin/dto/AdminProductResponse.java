package com.plavor.admin.dto;

import com.plavor.catalog.domain.Product;
import com.plavor.catalog.domain.ProductStatus;
import com.plavor.catalog.dto.CategoryResponse;
import com.plavor.catalog.dto.ProductImageResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "관리자 상품 응답")
public record AdminProductResponse(
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
		List<ProductImageResponse> images,

		@Schema(description = "생성 일시")
		LocalDateTime createdAt,

		@Schema(description = "수정 일시")
		LocalDateTime updatedAt
) {

	public static AdminProductResponse from(Product product) {
		return new AdminProductResponse(
				product.getId(),
				CategoryResponse.from(product.getCategory()),
				product.getName(),
				product.getDescription(),
				product.getPrice(),
				product.getStockQuantity(),
				product.getStatus(),
				product.getImages().stream()
						.map(ProductImageResponse::from)
						.toList(),
				product.getCreatedAt(),
				product.getUpdatedAt()
		);
	}
}
