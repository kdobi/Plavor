package com.plavor.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "주문 생성 요청")
public record OrderCreateRequest(
		@Schema(description = "주문할 장바구니 상품 ID 목록", example = "[1, 2]")
		@NotEmpty(message = "주문할 장바구니 상품을 선택해야 합니다.")
		List<@NotNull(message = "장바구니 상품 ID는 필수입니다.") @Positive(message = "장바구니 상품 ID는 양수여야 합니다.") Long> cartItemIds,

		@Schema(description = "수령자 이름", example = "김동빈")
		@NotBlank(message = "수령자 이름은 필수입니다.")
		@Size(max = 100, message = "수령자 이름은 100자 이하여야 합니다.")
		String receiverName,

		@Schema(description = "수령자 연락처", example = "01012345678")
		@NotBlank(message = "수령자 연락처는 필수입니다.")
		@Size(max = 30, message = "수령자 연락처는 30자 이하여야 합니다.")
		String receiverPhone,

		@Schema(description = "우편번호", example = "06236")
		@NotBlank(message = "우편번호는 필수입니다.")
		@Size(max = 20, message = "우편번호는 20자 이하여야 합니다.")
		String postalCode,

		@Schema(description = "기본 주소", example = "서울특별시 강남구 테헤란로 123")
		@NotBlank(message = "기본 주소는 필수입니다.")
		@Size(max = 255, message = "기본 주소는 255자 이하여야 합니다.")
		String address,

		@Schema(description = "상세 주소", example = "10층")
		@Size(max = 255, message = "상세 주소는 255자 이하여야 합니다.")
		String addressDetail,

		@Schema(description = "배송 요청사항", example = "문 앞에 놓아주세요.")
		@Size(max = 255, message = "배송 요청사항은 255자 이하여야 합니다.")
		String deliveryMessage
) {
}
