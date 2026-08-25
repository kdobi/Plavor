package com.plavor.admin.controller;

import com.plavor.admin.dto.AdminProductRequest;
import com.plavor.admin.dto.AdminProductResponse;
import com.plavor.admin.dto.AdminProductStatusUpdateRequest;
import com.plavor.admin.service.AdminProductService;
import com.plavor.catalog.domain.ProductStatus;
import com.plavor.global.common.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Product", description = "관리자 상품 관리 API")
@Validated
@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {

	private final AdminProductService adminProductService;

	public AdminProductController(AdminProductService adminProductService) {
		this.adminProductService = adminProductService;
	}

	@Operation(summary = "관리자 상품 목록 조회", description = "관리자가 전체 상품을 상태와 관계없이 페이지 단위로 조회합니다.")
	@GetMapping
	public PageResponse<AdminProductResponse> getProducts(
			@Parameter(description = "카테고리 ID")
			@RequestParam(required = false) Long categoryId,

			@Parameter(description = "상품 판매 상태")
			@RequestParam(required = false) ProductStatus status,

			@Parameter(description = "상품명 검색어")
			@RequestParam(required = false) String keyword,

			@Parameter(description = "페이지 번호. 0부터 시작합니다.", example = "0")
			@PositiveOrZero @RequestParam(defaultValue = "0") int page,

			@Parameter(description = "페이지 크기", example = "20")
			@Min(1) @Max(100) @RequestParam(defaultValue = "20") int size
	) {
		return adminProductService.getProducts(categoryId, status, keyword, page, size);
	}

	@Operation(summary = "관리자 상품 상세 조회", description = "관리자가 상품 ID로 상품 상세 정보를 조회합니다.")
	@GetMapping("/{productId}")
	public AdminProductResponse getProduct(
			@Parameter(description = "상품 ID", example = "1")
			@Positive @PathVariable Long productId
	) {
		return adminProductService.getProduct(productId);
	}

	@Operation(summary = "관리자 상품 등록", description = "관리자가 새 상품과 상품 이미지를 등록합니다.")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public AdminProductResponse createProduct(@Valid @RequestBody AdminProductRequest request) {
		return adminProductService.createProduct(request);
	}

	@Operation(summary = "관리자 상품 수정", description = "관리자가 상품 기본 정보와 이미지를 수정합니다.")
	@PutMapping("/{productId}")
	public AdminProductResponse updateProduct(
			@Parameter(description = "상품 ID", example = "1")
			@Positive @PathVariable Long productId,

			@Valid @RequestBody AdminProductRequest request
	) {
		return adminProductService.updateProduct(productId, request);
	}

	@Operation(summary = "관리자 상품 상태 변경", description = "관리자가 상품 판매 상태만 빠르게 변경합니다.")
	@PatchMapping("/{productId}/status")
	public AdminProductResponse updateStatus(
			@Parameter(description = "상품 ID", example = "1")
			@Positive @PathVariable Long productId,

			@Valid @RequestBody AdminProductStatusUpdateRequest request
	) {
		return adminProductService.updateStatus(productId, request);
	}
}
