package com.plavor.backend.product;

import java.util.List;

public record ProductDetailResponse(
		Long id,
		String name,
		String slug,
		String description,
		Long price,
		Integer stockQuantity,
		ProductStatus status,
		CategoryResponse category,
		List<ProductImageResponse> images
) {

	public static ProductDetailResponse from(Product product) {
		return new ProductDetailResponse(
				product.getId(),
				product.getName(),
				product.getSlug(),
				product.getDescription(),
				product.getPrice(),
				product.getStockQuantity(),
				product.getStatus(),
				CategoryResponse.from(product.getCategory()),
				product.getImages().stream()
						.map(ProductImageResponse::from)
						.toList()
		);
	}
}
