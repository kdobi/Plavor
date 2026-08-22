package com.plavor.catalog.dto;

import com.plavor.catalog.domain.Category;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "카테고리 응답")
public record CategoryResponse(
		@Schema(description = "카테고리 ID", example = "1")
		Long id,

		@Schema(description = "카테고리명", example = "Tops")
		String name,

		@Schema(description = "카테고리 URL 식별자", example = "tops")
		String slug,

		@Schema(description = "노출 순서", example = "1")
		int displayOrder
) {

	public static CategoryResponse from(Category category) {
		return new CategoryResponse(
				category.getId(),
				category.getName(),
				category.getSlug(),
				category.getDisplayOrder()
		);
	}
}
