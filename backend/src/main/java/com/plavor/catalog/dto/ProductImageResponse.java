package com.plavor.catalog.dto;

import com.plavor.catalog.domain.ProductImage;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 이미지 응답")
public record ProductImageResponse(
		@Schema(description = "이미지 ID", example = "1")
		Long id,

		@Schema(description = "이미지 URL", example = "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab")
		String imageUrl,

		@Schema(description = "이미지 대체 텍스트", example = "Minimal Cotton T-Shirt")
		String altText,

		@Schema(description = "노출 순서", example = "1")
		int displayOrder,

		@Schema(description = "대표 이미지 여부", example = "true")
		boolean thumbnail
) {

	public static ProductImageResponse from(ProductImage image) {
		return new ProductImageResponse(
				image.getId(),
				image.getImageUrl(),
				image.getAltText(),
				image.getDisplayOrder(),
				image.isThumbnail()
		);
	}
}
