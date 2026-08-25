package com.plavor.order.dto;

import com.plavor.catalog.domain.Product;
import com.plavor.catalog.domain.ProductImage;
import com.plavor.order.domain.OrderItem;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주문 상품 응답")
public record OrderItemResponse(
		@Schema(description = "주문 상품 ID", example = "1")
		Long id,

		@Schema(description = "상품 ID", example = "1")
		Long productId,

		@Schema(description = "주문 당시 상품명", example = "Minimal Cotton T-Shirt")
		String productName,

		@Schema(description = "상품 대표 이미지 URL", example = "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab")
		String thumbnailImageUrl,

		@Schema(description = "주문 당시 상품 단가", example = "29000")
		long unitPrice,

		@Schema(description = "주문 수량", example = "2")
		int quantity,

		@Schema(description = "주문 상품 합계 금액", example = "58000")
		long totalPrice
) {

	public static OrderItemResponse from(OrderItem item) {
		Product product = item.getProduct();

		return new OrderItemResponse(
				item.getId(),
				product.getId(),
				item.getProductName(),
				findThumbnailImageUrl(product),
				item.getUnitPrice(),
				item.getQuantity(),
				item.getTotalPrice()
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
