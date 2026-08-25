package com.plavor.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "장바구니 상품 수량 변경 요청")
public record CartItemUpdateRequest(
		@Schema(description = "변경할 수량", example = "3")
		@NotNull(message = "수량은 필수입니다.")
		@Positive(message = "수량은 1개 이상이어야 합니다.")
		Integer quantity
) {
}
