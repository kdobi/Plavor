package com.plavor.backend.product;

public record ProductSummaryResponse(
		Long id,
		String name,
		String slug,
		Long price,
		Integer stockQuantity,
		ProductStatus status,
		CategoryResponse category,
		String thumbnailImageUrl
) {

	public static ProductSummaryResponse from(Product product) {
		return new ProductSummaryResponse(
				product.getId(),
				product.getName(),
				product.getSlug(),
				product.getPrice(),
				product.getStockQuantity(),
				product.getStatus(),
				CategoryResponse.from(product.getCategory()),
				ProductImageSelector.thumbnailUrl(product)
		);
	}
}
