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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

	@Operation(summary = "내 주문 목록 조회", description = "로그인한 회원의 주문 목록을 최신순으로 조회합니다.")
	@GetMapping
	public List<OrderResponse> getOrders(
			@AuthenticationPrincipal AuthenticatedMember authenticatedMember
	) {
		return orderService.getOrders(authenticatedMember.id());
	}

	@Operation(summary = "내 주문 상세 조회", description = "로그인한 회원의 특정 주문 상세 정보를 조회합니다.")
	@GetMapping("/{orderId}")
	public OrderResponse getOrder(
			@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
			@PathVariable Long orderId
	) {
		return orderService.getOrder(authenticatedMember.id(), orderId);
	}
}
