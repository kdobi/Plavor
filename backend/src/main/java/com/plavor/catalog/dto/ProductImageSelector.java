package com.plavor.catalog.dto;

import com.plavor.catalog.domain.Product;
import com.plavor.catalog.domain.ProductImage;

final class ProductImageSelector {

	private ProductImageSelector() {
	}

	static String thumbnailUrl(Product product) {
		return product.getImages().stream()
				.filter(image -> Boolean.TRUE.equals(image.getThumbnail()))
				.findFirst()
				.or(() -> product.getImages().stream().findFirst())
				.map(ProductImage::getImageUrl)
				.orElse(null);
	}
}
