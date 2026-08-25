package com.plavor.global.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

	COMMON_INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_INVALID_INPUT", "요청 값이 올바르지 않습니다."),
	COMMON_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_NOT_FOUND", "요청한 리소스를 찾을 수 없습니다."),
	COMMON_METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_METHOD_NOT_ALLOWED", "허용되지 않은 HTTP 메서드입니다."),
	COMMON_INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다."),

	AUTH_EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH_EMAIL_ALREADY_EXISTS", "이미 사용 중인 이메일입니다."),
	AUTH_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다."),
	AUTH_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_UNAUTHORIZED", "로그인이 필요합니다."),
	AUTH_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_TOKEN", "유효하지 않은 인증 토큰입니다."),
	AUTH_INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_REFRESH_TOKEN", "로그인 세션이 만료되었습니다."),
	AUTH_INVALID_OAUTH_STATE(HttpStatus.BAD_REQUEST, "AUTH_INVALID_OAUTH_STATE", "소셜 로그인 요청이 만료되었거나 올바르지 않습니다."),
	AUTH_KAKAO_LOGIN_FAILED(HttpStatus.BAD_GATEWAY, "AUTH_KAKAO_LOGIN_FAILED", "카카오 로그인 처리에 실패했습니다."),

	CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "CART_ITEM_NOT_FOUND", "장바구니 상품을 찾을 수 없습니다."),
	CART_PRODUCT_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "CART_PRODUCT_NOT_AVAILABLE", "장바구니에 담을 수 없는 상품입니다."),
	CART_QUANTITY_EXCEEDS_STOCK(HttpStatus.BAD_REQUEST, "CART_QUANTITY_EXCEEDS_STOCK", "상품 재고보다 많은 수량을 담을 수 없습니다."),

	ORDER_EMPTY_ITEM_SELECTION(HttpStatus.BAD_REQUEST, "ORDER_EMPTY_ITEM_SELECTION", "주문할 장바구니 상품을 선택해야 합니다."),
	ORDER_CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_CART_ITEM_NOT_FOUND", "주문할 장바구니 상품을 찾을 수 없습니다."),
	ORDER_PRODUCT_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "ORDER_PRODUCT_NOT_AVAILABLE", "주문할 수 없는 상품이 포함되어 있습니다."),
	ORDER_QUANTITY_EXCEEDS_STOCK(HttpStatus.BAD_REQUEST, "ORDER_QUANTITY_EXCEEDS_STOCK", "주문 수량이 상품 재고보다 많습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;

	ErrorCode(HttpStatus status, String code, String message) {
		this.status = status;
		this.code = code;
		this.message = message;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}
}
