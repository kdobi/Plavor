package com.plavor.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Schema(description = "관리자 상품 이미지 요청")
public record AdminProductImageRequest(
		@Schema(description = "이미지 URL", example = "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab")
		@NotBlank
		@Size(max = 500)
		String imageUrl,

		@Schema(description = "이미지 대체 텍스트", example = "Minimal Cotton T-Shirt")
		@Size(max = 255)
		String altText,

		@Schema(description = "노출 순서", example = "1")
		@PositiveOrZero
		int displayOrder,

		@Schema(description = "대표 이미지 여부", example = "true")
		boolean thumbnail
) {
}
