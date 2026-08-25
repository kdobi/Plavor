package com.plavor.admin.controller;

import com.plavor.admin.dto.AdminOrderResponse;
import com.plavor.admin.dto.AdminOrderStatusUpdateRequest;
import com.plavor.admin.service.AdminOrderService;
import com.plavor.global.common.PageResponse;
import com.plavor.order.domain.OrderStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Order", description = "관리자 주문 관리 API")
@Validated
@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

	private final AdminOrderService adminOrderService;

	public AdminOrderController(AdminOrderService adminOrderService) {
		this.adminOrderService = adminOrderService;
	}

	@Operation(summary = "관리자 주문 목록 조회", description = "관리자가 전체 주문을 상태와 검색어로 조회합니다.")
	@GetMapping
	public PageResponse<AdminOrderResponse> getOrders(
			@Parameter(description = "주문 상태")
			@RequestParam(required = false) OrderStatus status,

			@Parameter(description = "주문번호, 수령자, 연락처, 회원 이메일/이름 검색어")
			@RequestParam(required = false) String keyword,

			@Parameter(description = "페이지 번호. 0부터 시작합니다.", example = "0")
			@PositiveOrZero @RequestParam(defaultValue = "0") int page,

			@Parameter(description = "페이지 크기", example = "20")
			@Min(1) @Max(100) @RequestParam(defaultValue = "20") int size
	) {
		return adminOrderService.getOrders(status, keyword, page, size);
	}

	@Operation(summary = "관리자 주문 상세 조회", description = "관리자가 주문 ID로 주문 상세 정보를 조회합니다.")
	@GetMapping("/{orderId}")
	public AdminOrderResponse getOrder(
			@Parameter(description = "주문 ID", example = "1")
			@Positive @PathVariable Long orderId
	) {
		return adminOrderService.getOrder(orderId);
	}

	@Operation(summary = "관리자 주문 상태 변경", description = "관리자가 주문 처리 상태를 변경합니다.")
	@PatchMapping("/{orderId}/status")
	public AdminOrderResponse updateStatus(
			@Parameter(description = "주문 ID", example = "1")
			@Positive @PathVariable Long orderId,

			@Valid @RequestBody AdminOrderStatusUpdateRequest request
	) {
		return adminOrderService.updateStatus(orderId, request);
	}
}
