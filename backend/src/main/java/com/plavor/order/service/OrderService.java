package com.plavor.order.service;

import com.plavor.cart.domain.CartItem;
import com.plavor.cart.repository.CartItemRepository;
import com.plavor.catalog.domain.Product;
import com.plavor.catalog.domain.ProductStatus;
import com.plavor.global.error.BusinessException;
import com.plavor.global.error.ErrorCode;
import com.plavor.member.domain.Member;
import com.plavor.member.repository.MemberRepository;
import com.plavor.order.domain.Order;
import com.plavor.order.dto.OrderCreateRequest;
import com.plavor.order.dto.OrderResponse;
import com.plavor.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
public class OrderService {

	private static final DateTimeFormatter ORDER_NUMBER_DATE_FORMAT =
			DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

	private final OrderRepository orderRepository;
	private final CartItemRepository cartItemRepository;
	private final MemberRepository memberRepository;

	public OrderService(
			OrderRepository orderRepository,
			CartItemRepository cartItemRepository,
			MemberRepository memberRepository
	) {
		this.orderRepository = orderRepository;
		this.cartItemRepository = cartItemRepository;
		this.memberRepository = memberRepository;
	}

	public OrderResponse createOrder(Long memberId, OrderCreateRequest request) {
		List<Long> cartItemIds = new ArrayList<>(new LinkedHashSet<>(request.cartItemIds()));

		if (cartItemIds.isEmpty()) {
			throw new BusinessException(ErrorCode.ORDER_EMPTY_ITEM_SELECTION);
		}

		List<CartItem> cartItems = cartItemRepository.findAllByMemberIdAndIdInWithProduct(memberId, cartItemIds);
		if (cartItems.size() != cartItemIds.size()) {
			throw new BusinessException(ErrorCode.ORDER_CART_ITEM_NOT_FOUND);
		}

		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "회원을 찾을 수 없습니다."));

		Order order = new Order(
				member,
				createOrderNumber(),
				request.receiverName().trim(),
				request.receiverPhone().trim(),
				request.postalCode().trim(),
				request.address().trim(),
				normalizeNullable(request.addressDetail()),
				normalizeNullable(request.deliveryMessage())
		);

		for (CartItem cartItem : cartItems) {
			Product product = cartItem.getProduct();

			validateOrderableProduct(product, cartItem.getQuantity());
			order.addItem(product, cartItem.getQuantity());
			product.decreaseStock(cartItem.getQuantity());
		}

		Order savedOrder = orderRepository.save(order);
		cartItems.forEach(cartItem -> cartItem.getCart().removeItem(cartItem));
		orderRepository.flush();

		return OrderResponse.from(savedOrder);
	}

	@Transactional(readOnly = true)
	public List<OrderResponse> getOrders(Long memberId) {
		return orderRepository.findAllByMemberIdWithItems(memberId).stream()
				.map(OrderResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public OrderResponse getOrder(Long memberId, Long orderId) {
		Order order = orderRepository.findByIdAndMemberIdWithItems(orderId, memberId)
				.orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

		return OrderResponse.from(order);
	}

	private void validateOrderableProduct(Product product, int quantity) {
		if (product.getStatus() != ProductStatus.ACTIVE || !product.getCategory().isActive()) {
			throw new BusinessException(ErrorCode.ORDER_PRODUCT_NOT_AVAILABLE);
		}

		if (!product.hasStock(quantity)) {
			throw new BusinessException(ErrorCode.ORDER_QUANTITY_EXCEEDS_STOCK);
		}
	}

	private String createOrderNumber() {
		String timestamp = LocalDateTime.now().format(ORDER_NUMBER_DATE_FORMAT);
		String randomSuffix = UUID.randomUUID()
				.toString()
				.replace("-", "")
				.substring(0, 8)
				.toUpperCase(Locale.ROOT);

		return "PLV" + timestamp + randomSuffix;
	}

	private String normalizeNullable(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}

		return value.trim();
	}
}
