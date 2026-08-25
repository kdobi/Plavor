package com.plavor.order.controller;

import com.plavor.global.security.AuthenticatedMember;
import com.plavor.order.dto.OrderCreateRequest;
import com.plavor.order.dto.OrderResponse;
import com.plavor.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Order", description = "주문 API")
@RestController
@RequestMapping("/api/orders")
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@Operation(summary = "주문 생성", description = "선택한 장바구니 상품과 배송지 정보로 주문을 생성합니다.")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public OrderResponse createOrder(
			@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
			@Valid @RequestBody OrderCreateRequest request
	) {
		return orderService.createOrder(authenticatedMember.id(), request);
	}
}
