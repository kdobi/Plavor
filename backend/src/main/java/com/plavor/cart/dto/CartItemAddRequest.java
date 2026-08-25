package com.plavor.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "장바구니 상품 담기 요청")
public record CartItemAddRequest(
		@Schema(description = "상품 ID", example = "1")
		@NotNull(message = "상품 ID는 필수입니다.")
		@Positive(message = "상품 ID는 양수여야 합니다.")
		Long productId,

		@Schema(description = "담을 수량", example = "2")
		@NotNull(message = "수량은 필수입니다.")
		@Positive(message = "수량은 1개 이상이어야 합니다.")
		Integer quantity
) {
}
