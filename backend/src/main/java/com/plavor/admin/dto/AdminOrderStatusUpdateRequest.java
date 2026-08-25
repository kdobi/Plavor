package com.plavor.admin.dto;

import com.plavor.order.domain.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "관리자 주문 상태 변경 요청")
public record AdminOrderStatusUpdateRequest(
		@Schema(description = "주문 상태", example = "SHIPPED")
		@NotNull
		OrderStatus status
) {
}
