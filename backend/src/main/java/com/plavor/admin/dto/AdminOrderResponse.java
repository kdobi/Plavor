package com.plavor.admin.dto;

import com.plavor.member.domain.Member;
import com.plavor.order.domain.Order;
import com.plavor.order.domain.OrderStatus;
import com.plavor.order.dto.OrderItemResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "관리자 주문 응답")
public record AdminOrderResponse(
		@Schema(description = "주문 ID", example = "1")
		Long id,

		@Schema(description = "주문 번호", example = "PLV202608251130001A2B3C4D")
		String orderNumber,

		@Schema(description = "주문 상태", example = "PREPARING")
		OrderStatus status,

		@Schema(description = "주문 상품 총 금액", example = "58000")
		long totalAmount,

		@Schema(description = "회원 ID", example = "1")
		Long memberId,

		@Schema(description = "회원 이메일", example = "customer@example.com")
		String memberEmail,

		@Schema(description = "회원 이름", example = "김동빈")
		String memberName,

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

		@Schema(description = "주문 수정 시각")
		LocalDateTime updatedAt,

		@Schema(description = "주문 상품 목록")
		List<OrderItemResponse> items
) {

	public static AdminOrderResponse from(Order order) {
		Member member = order.getMember();

		return new AdminOrderResponse(
				order.getId(),
				order.getOrderNumber(),
				order.getStatus(),
				order.getTotalAmount(),
				member.getId(),
				member.getEmail(),
				member.getName(),
				order.getReceiverName(),
				order.getReceiverPhone(),
				order.getPostalCode(),
				order.getAddress(),
				order.getAddressDetail(),
				order.getDeliveryMessage(),
				order.getOrderedAt(),
				order.getUpdatedAt(),
				order.getItems().stream()
						.map(OrderItemResponse::from)
						.toList()
		);
	}
}
