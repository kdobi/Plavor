package com.plavor.catalog.dto;

import com.plavor.catalog.domain.ProductImage;

public record ProductImageResponse(
		Long id,
		String imageUrl,
		String altText,
		Integer displayOrder,
		Boolean thumbnail
) {

	public static ProductImageResponse from(ProductImage image) {
		return new ProductImageResponse(
				image.getId(),
				image.getImageUrl(),
				image.getAltText(),
				image.getDisplayOrder(),
				image.getThumbnail()
		);
	}
}
