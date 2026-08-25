package com.plavor.admin.dto;

import com.plavor.catalog.domain.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "관리자 상품 저장 요청")
public record AdminProductRequest(
		@Schema(description = "카테고리 ID", example = "1")
		@NotNull
		@Positive
		Long categoryId,

		@Schema(description = "상품명", example = "Minimal Cotton T-Shirt")
		@NotBlank
		@Size(max = 200)
		String name,

		@Schema(description = "상품 설명", example = "A clean everyday cotton T-shirt with a relaxed silhouette.")
		@Size(max = 5000)
		String description,

		@Schema(description = "상품 가격", example = "29000")
		@PositiveOrZero
		long price,

		@Schema(description = "재고 수량", example = "120")
		@PositiveOrZero
		int stockQuantity,

		@Schema(description = "상품 판매 상태", example = "ACTIVE")
		@NotNull
		ProductStatus status,

		@Schema(description = "상품 이미지 목록")
		@Size(max = 10)
		List<@Valid AdminProductImageRequest> images
) {
}
