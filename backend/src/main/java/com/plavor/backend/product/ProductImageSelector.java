package com.plavor.backend.product;

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
