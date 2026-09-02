package com.plavor.admin.controller;

import com.plavor.admin.dto.AdminImageUploadResponse;
import com.plavor.admin.service.AdminImageUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Admin Upload", description = "관리자 파일 업로드 API")
@Validated
@RestController
@RequestMapping("/api/admin/uploads")
public class AdminUploadController {

	private final AdminImageUploadService adminImageUploadService;

	public AdminUploadController(AdminImageUploadService adminImageUploadService) {
		this.adminImageUploadService = adminImageUploadService;
	}

	@Operation(summary = "관리자 상품 이미지 업로드", description = "관리자가 상품 등록/수정에 사용할 이미지를 업로드합니다.")
	@PostMapping("/images")
	@ResponseStatus(HttpStatus.CREATED)
	public AdminImageUploadResponse uploadImage(@NotNull @RequestParam("file") MultipartFile file) {
		return adminImageUploadService.uploadProductImage(file);
	}
}
