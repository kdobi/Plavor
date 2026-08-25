package com.plavor.order.dto;

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

		@Schema(description = "주문 당시 상품 단가", example = "29000")
		long unitPrice,

		@Schema(description = "주문 수량", example = "2")
		int quantity,

		@Schema(description = "주문 상품 합계 금액", example = "58000")
		long totalPrice
) {

	public static OrderItemResponse from(OrderItem item) {
		return new OrderItemResponse(
				item.getId(),
				item.getProduct().getId(),
				item.getProductName(),
				item.getUnitPrice(),
				item.getQuantity(),
				item.getTotalPrice()
		);
	}
}
