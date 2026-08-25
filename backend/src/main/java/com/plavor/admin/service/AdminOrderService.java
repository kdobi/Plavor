package com.plavor.admin.service;

import com.plavor.admin.dto.AdminOrderResponse;
import com.plavor.admin.dto.AdminOrderStatusUpdateRequest;
import com.plavor.global.common.PageResponse;
import com.plavor.global.error.BusinessException;
import com.plavor.global.error.ErrorCode;
import com.plavor.order.domain.Order;
import com.plavor.order.domain.OrderStatus;
import com.plavor.order.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminOrderService {

	private final OrderRepository orderRepository;

	public AdminOrderService(OrderRepository orderRepository) {
		this.orderRepository = orderRepository;
	}

	@Transactional(readOnly = true)
	public PageResponse<AdminOrderResponse> getOrders(OrderStatus status, String keyword, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<Long> orderIds = orderRepository.findAdminOrderIds(status, normalizeKeyword(keyword), pageable);

		if (orderIds.isEmpty()) {
			return PageResponse.from(new PageImpl<>(List.of(), pageable, orderIds.getTotalElements()));
		}

		List<Order> orders = orderRepository.findAllAdminOrdersWithDetails(orderIds.getContent());
		Map<Long, Order> ordersById = orders.stream()
				.collect(Collectors.toMap(Order::getId, Function.identity()));
		List<AdminOrderResponse> content = orderIds.getContent().stream()
				.map(ordersById::get)
				.filter(Objects::nonNull)
				.map(AdminOrderResponse::from)
				.toList();

		return PageResponse.from(new PageImpl<>(content, pageable, orderIds.getTotalElements()));
	}

	@Transactional(readOnly = true)
	public AdminOrderResponse getOrder(Long orderId) {
		return AdminOrderResponse.from(getOrderWithDetails(orderId));
	}

	public AdminOrderResponse updateStatus(Long orderId, AdminOrderStatusUpdateRequest request) {
		Order order = getOrderWithDetails(orderId);

		order.updateStatus(request.status());
		orderRepository.flush();

		return AdminOrderResponse.from(order);
	}

	private Order getOrderWithDetails(Long orderId) {
		return orderRepository.findAdminOrderByIdWithDetails(orderId)
				.orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
	}

	private String normalizeKeyword(String keyword) {
		if (keyword == null || keyword.isBlank()) {
			return "";
		}

		return keyword.trim().toLowerCase(Locale.ROOT);
	}
}
