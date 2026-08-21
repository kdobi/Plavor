package com.plavor.catalog.dto;

import com.plavor.catalog.domain.Category;

public record CategoryResponse(
		Long id,
		String name,
		String slug
) {

	public static CategoryResponse from(Category category) {
		return new CategoryResponse(
				category.getId(),
				category.getName(),
				category.getSlug()
		);
	}
}
