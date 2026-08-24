package com.plavor.catalog.controller;

import com.plavor.catalog.dto.CategoryResponse;
import com.plavor.catalog.dto.ProductDetailResponse;
import com.plavor.catalog.dto.ProductSummaryResponse;
import com.plavor.catalog.service.CatalogService;
import com.plavor.global.common.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Catalog", description = "상품 카탈로그 공개 API")
@Validated
@RestController
@RequestMapping("/api")
public class CatalogController {

	private final CatalogService catalogService;

	public CatalogController(CatalogService catalogService) {
		this.catalogService = catalogService;
	}

	@Operation(summary = "상품 목록 조회", description = "공개 상태의 상품 목록을 페이지 단위로 조회합니다.")
	@GetMapping("/products")
	public PageResponse<ProductSummaryResponse> getProducts(
			@Parameter(description = "카테고리 ID")
			@RequestParam(required = false) Long categoryId,

			@Parameter(description = "상품명 검색어")
			@RequestParam(required = false) String keyword,

			@Parameter(description = "페이지 번호. 0부터 시작합니다.", example = "0")
			@PositiveOrZero @RequestParam(defaultValue = "0") int page,

			@Parameter(description = "페이지 크기", example = "20")
			@Min(1) @Max(100) @RequestParam(defaultValue = "20") int size
	) {
		return catalogService.getProducts(categoryId, keyword, page, size);
	}

	@Operation(summary = "상품 상세 조회", description = "상품 ID로 공개 상품 상세 정보를 조회합니다.")
	@GetMapping("/products/{productId}")
	public ProductDetailResponse getProduct(
			@Parameter(description = "상품 ID", example = "1")
			@Positive @PathVariable Long productId
	) {
		return catalogService.getProduct(productId);
	}

	@Operation(summary = "카테고리 목록 조회", description = "활성화된 카테고리 목록을 노출 순서대로 조회합니다.")
	@GetMapping("/categories")
	public List<CategoryResponse> getCategories() {
		return catalogService.getCategories();
	}
}
