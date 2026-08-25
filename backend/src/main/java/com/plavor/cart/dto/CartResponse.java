package com.plavor.cart.dto;

import com.plavor.cart.domain.Cart;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "장바구니 응답")
public record CartResponse(
		@Schema(description = "장바구니 ID", example = "1")
		Long id,

		@Schema(description = "장바구니 상품 목록")
		List<CartItemResponse> items,

		@Schema(description = "총 상품 수량", example = "2")
		int totalQuantity,

		@Schema(description = "총 주문 예정 금액", example = "58000")
		long totalAmount
) {

	public static CartResponse from(Cart cart) {
		List<CartItemResponse> items = cart.getItems().stream()
				.map(CartItemResponse::from)
				.toList();

		return new CartResponse(
				cart.getId(),
				items,
				items.stream().mapToInt(CartItemResponse::quantity).sum(),
				items.stream().mapToLong(CartItemResponse::totalPrice).sum()
		);
	}
}
