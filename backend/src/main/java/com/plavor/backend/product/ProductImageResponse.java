package com.plavor.backend.product;

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
