package com.plavor.order.dto;

import com.plavor.order.domain.Order;
import com.plavor.order.domain.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "주문 응답")
public record OrderResponse(
		@Schema(description = "주문 ID", example = "1")
		Long id,

		@Schema(description = "주문 번호", example = "PLV202608251130001A2B3C4D")
		String orderNumber,

		@Schema(description = "주문 상태", example = "CREATED")
		OrderStatus status,

		@Schema(description = "주문 상품 총 금액", example = "58000")
		long totalAmount,

		@Schema(description = "수령자 이름", example = "김동빈")
		String receiverName,

		@Schema(description = "수령자 연락처", example = "01012345678")
		String receiverPhone,

		@Schema(description = "우편번호", example = "06236")
		String postalCode,

		@Schema(description = "기본 주소", example = "서울특별시 강남구 테헤란로 123")
		String address,

		@Schema(description = "상세 주소", example = "10층")
		String addressDetail,

		@Schema(description = "배송 요청사항", example = "문 앞에 놓아주세요.")
		String deliveryMessage,

		@Schema(description = "주문 생성 시각")
		LocalDateTime orderedAt,

		@Schema(description = "주문 상품 목록")
		List<OrderItemResponse> items
) {

	public static OrderResponse from(Order order) {
		return new OrderResponse(
				order.getId(),
				order.getOrderNumber(),
				order.getStatus(),
				order.getTotalAmount(),
				order.getReceiverName(),
				order.getReceiverPhone(),
				order.getPostalCode(),
				order.getAddress(),
				order.getAddressDetail(),
				order.getDeliveryMessage(),
				order.getOrderedAt(),
				order.getItems().stream()
						.map(OrderItemResponse::from)
						.toList()
		);
	}
}
