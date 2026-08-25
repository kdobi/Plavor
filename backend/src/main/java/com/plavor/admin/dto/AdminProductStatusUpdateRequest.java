package com.plavor.admin.dto;

import com.plavor.catalog.domain.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "관리자 상품 상태 변경 요청")
public record AdminProductStatusUpdateRequest(
		@Schema(description = "상품 판매 상태", example = "HIDDEN")
		@NotNull
		ProductStatus status
) {
}
