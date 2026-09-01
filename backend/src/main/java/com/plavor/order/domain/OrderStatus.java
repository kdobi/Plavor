package com.plavor.order.domain;

import java.util.List;

public enum OrderStatus {
	CREATED,
	PAID,
	PREPARING,
	SHIPPED,
	DELIVERED,
	CANCELED;

	public boolean canTransitionTo(OrderStatus nextStatus) {
		if (nextStatus == null || this == nextStatus) {
			return false;
		}

		return availableNextStatuses().contains(nextStatus);
	}

	public List<OrderStatus> availableNextStatuses() {
		return switch (this) {
			case CREATED -> List.of(PAID, PREPARING, CANCELED);
			case PAID -> List.of(PREPARING, CANCELED);
			case PREPARING -> List.of(SHIPPED, CANCELED);
			case SHIPPED -> List.of(DELIVERED);
			case DELIVERED, CANCELED -> List.of();
		};
	}

	public String getLabel() {
		return switch (this) {
			case CREATED -> "주문 접수";
			case PAID -> "결제 완료";
			case PREPARING -> "배송 준비";
			case SHIPPED -> "배송 중";
			case DELIVERED -> "배송 완료";
			case CANCELED -> "주문 취소";
		};
	}
}
