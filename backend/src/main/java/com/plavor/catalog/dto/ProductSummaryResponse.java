package com.plavor.catalog.dto;

import com.plavor.catalog.domain.Product;
import com.plavor.catalog.domain.ProductImage;
import com.plavor.catalog.domain.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 목록 응답")
public record ProductSummaryResponse(
		@Schema(description = "상품 ID", example = "1")
		Long id,

		@Schema(description = "카테고리 ID", example = "1")
		Long categoryId,

		@Schema(description = "카테고리명", example = "Tops")
		String categoryName,

		@Schema(description = "카테고리 URL 식별자", example = "tops")
		String categorySlug,

		@Schema(description = "상품명", example = "Minimal Cotton T-Shirt")
		String name,

		@Schema(description = "상품 가격", example = "29000")
		long price,

		@Schema(description = "재고 수량", example = "120")
		int stockQuantity,

		@Schema(description = "상품 판매 상태", example = "ACTIVE")
		ProductStatus status,

		@Schema(description = "대표 이미지")
		ProductImageResponse thumbnailImage
) {

	public static ProductSummaryResponse from(Product product) {
		return new ProductSummaryResponse(
				product.getId(),
				product.getCategory().getId(),
				product.getCategory().getName(),
				product.getCategory().getSlug(),
				product.getName(),
				product.getPrice(),
				product.getStockQuantity(),
				product.getStatus(),
				findThumbnailImage(product)
		);
	}

	private static ProductImageResponse findThumbnailImage(Product product) {
		return product.getImages().stream()
				.filter(ProductImage::isThumbnail)
				.findFirst()
				.or(() -> product.getImages().stream().findFirst())
				.map(ProductImageResponse::from)
				.orElse(null);
	}
}
