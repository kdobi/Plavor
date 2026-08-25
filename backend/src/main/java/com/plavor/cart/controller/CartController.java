package com.plavor.cart.controller;

import com.plavor.cart.dto.CartItemAddRequest;
import com.plavor.cart.dto.CartItemUpdateRequest;
import com.plavor.cart.dto.CartResponse;
import com.plavor.cart.service.CartService;
import com.plavor.global.security.AuthenticatedMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Cart", description = "장바구니 API")
@Validated
@RestController
@RequestMapping("/api/cart")
public class CartController {

	private final CartService cartService;

	public CartController(CartService cartService) {
		this.cartService = cartService;
	}

	@Operation(summary = "내 장바구니 조회", description = "로그인한 회원의 장바구니 상품 목록과 합계 금액을 조회합니다.")
	@GetMapping
	public CartResponse getCart(@AuthenticationPrincipal AuthenticatedMember authenticatedMember) {
		return cartService.getCart(authenticatedMember.id());
	}

	@Operation(summary = "장바구니 상품 담기", description = "상품을 장바구니에 담습니다. 이미 담긴 상품이면 수량을 증가시킵니다.")
	@PostMapping("/items")
	@ResponseStatus(HttpStatus.CREATED)
	public CartResponse addItem(
			@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
			@Valid @RequestBody CartItemAddRequest request
	) {
		return cartService.addItem(authenticatedMember.id(), request);
	}

	@Operation(summary = "장바구니 상품 수량 변경", description = "장바구니 상품의 수량을 지정한 값으로 변경합니다.")
	@PatchMapping("/items/{itemId}")
	public CartResponse updateItem(
			@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
			@Parameter(description = "장바구니 상품 ID", example = "1")
			@Positive @PathVariable Long itemId,
			@Valid @RequestBody CartItemUpdateRequest request
	) {
		return cartService.updateItem(authenticatedMember.id(), itemId, request);
	}

	@Operation(summary = "장바구니 상품 삭제", description = "장바구니에서 상품을 삭제합니다.")
	@DeleteMapping("/items/{itemId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteItem(
			@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
			@Parameter(description = "장바구니 상품 ID", example = "1")
			@Positive @PathVariable Long itemId
	) {
		cartService.deleteItem(authenticatedMember.id(), itemId);
	}
}
