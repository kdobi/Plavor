package com.plavor.cart.dto;

import com.plavor.cart.domain.CartItem;
import com.plavor.catalog.domain.Product;
import com.plavor.catalog.domain.ProductImage;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "장바구니 상품 응답")
public record CartItemResponse(
		@Schema(description = "장바구니 상품 ID", example = "1")
		Long id,

		@Schema(description = "상품 ID", example = "1")
		Long productId,

		@Schema(description = "상품명", example = "Minimal Cotton T-Shirt")
		String productName,

		@Schema(description = "상품 대표 이미지 URL")
		String thumbnailImageUrl,

		@Schema(description = "상품 단가", example = "29000")
		long unitPrice,

		@Schema(description = "수량", example = "2")
		int quantity,

		@Schema(description = "상품 재고 수량", example = "120")
		int stockQuantity,

		@Schema(description = "아이템 합계 금액", example = "58000")
		long totalPrice
) {

	public static CartItemResponse from(CartItem item) {
		Product product = item.getProduct();

		return new CartItemResponse(
				item.getId(),
				product.getId(),
				product.getName(),
				findThumbnailImageUrl(product),
				product.getPrice(),
				item.getQuantity(),
				product.getStockQuantity(),
				item.calculateTotalPrice()
		);
	}

	private static String findThumbnailImageUrl(Product product) {
		return product.getImages().stream()
				.filter(ProductImage::isThumbnail)
				.findFirst()
				.or(() -> product.getImages().stream().findFirst())
				.map(ProductImage::getImageUrl)
				.orElse(null);
	}
}
