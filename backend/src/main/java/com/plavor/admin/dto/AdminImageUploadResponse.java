package com.plavor.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 이미지 업로드 응답")
public record AdminImageUploadResponse(
		@Schema(description = "브라우저에서 접근할 이미지 URL", example = "/uploads/products/2026/09/uuid.webp")
		String imageUrl,

		@Schema(description = "업로드한 원본 파일명", example = "hoodie.webp")
		String originalFilename,

		@Schema(description = "감지된 이미지 MIME 타입", example = "image/webp")
		String contentType,

		@Schema(description = "파일 크기", example = "204800")
		long size
) {
}
