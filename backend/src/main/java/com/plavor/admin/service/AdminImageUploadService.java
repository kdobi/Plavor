package com.plavor.admin.service;

import com.plavor.admin.dto.AdminImageUploadResponse;
import com.plavor.global.config.UploadProperties;
import com.plavor.global.error.BusinessException;
import com.plavor.global.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class AdminImageUploadService {

	private final UploadProperties uploadProperties;

	public AdminImageUploadService(UploadProperties uploadProperties) {
		this.uploadProperties = uploadProperties;
	}

	public AdminImageUploadResponse uploadProductImage(MultipartFile file) {
		validateFile(file);

		byte[] bytes = readBytes(file);
		validateSize(bytes.length);
		ImageFileType imageFileType = detectImageType(bytes)
				.orElseThrow(() -> new BusinessException(
						ErrorCode.UPLOAD_INVALID_IMAGE_TYPE,
						"jpg, png, webp 이미지만 업로드할 수 있습니다."
				));

		LocalDate today = LocalDate.now();
		String year = String.valueOf(today.getYear());
		String month = "%02d".formatted(today.getMonthValue());
		String filename = "%s.%s".formatted(UUID.randomUUID(), imageFileType.extension());
		Path rootDirectory = uploadProperties.getAbsoluteImageDirectory();
		Path targetDirectory = rootDirectory.resolve(Path.of("products", year, month)).normalize();
		Path targetPath = targetDirectory.resolve(filename).normalize();

		if (!targetDirectory.startsWith(rootDirectory) || !targetPath.startsWith(rootDirectory)) {
			throw new BusinessException(ErrorCode.UPLOAD_STORE_FAILED);
		}

		try {
			Files.createDirectories(targetDirectory);
			Files.write(targetPath, bytes, StandardOpenOption.CREATE_NEW);
		} catch (IOException exception) {
			throw new BusinessException(ErrorCode.UPLOAD_STORE_FAILED);
		}

		String imageUrl = "%s/products/%s/%s/%s".formatted(
				uploadProperties.getNormalizedPublicUrlPrefix(),
				year,
				month,
				filename
		);

		return new AdminImageUploadResponse(
				imageUrl,
				normalizeOriginalFilename(file.getOriginalFilename()),
				imageFileType.contentType(),
				bytes.length
		);
	}

	private void validateFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new BusinessException(ErrorCode.UPLOAD_EMPTY_FILE);
		}

		validateSize(file.getSize());
	}

	private void validateSize(long size) {
		if (size > uploadProperties.getMaxImageSizeBytes()) {
			long maxMegabytes = Math.max(1, uploadProperties.getMaxImageSizeBytes() / (1024L * 1024L));
			throw new BusinessException(
					ErrorCode.UPLOAD_IMAGE_TOO_LARGE,
					"이미지는 %dMB 이하로 업로드해주세요.".formatted(maxMegabytes)
			);
		}
	}

	private byte[] readBytes(MultipartFile file) {
		try {
			return file.getBytes();
		} catch (IOException exception) {
			throw new BusinessException(ErrorCode.UPLOAD_STORE_FAILED);
		}
	}

	private Optional<ImageFileType> detectImageType(byte[] bytes) {
		if (startsWith(bytes, new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF})) {
			return Optional.of(ImageFileType.JPEG);
		}

		if (startsWith(bytes, new byte[] {
				(byte) 0x89, 0x50, 0x4E, 0x47,
				0x0D, 0x0A, 0x1A, 0x0A
		})) {
			return Optional.of(ImageFileType.PNG);
		}

		if (bytes.length >= 12
				&& bytes[0] == 0x52
				&& bytes[1] == 0x49
				&& bytes[2] == 0x46
				&& bytes[3] == 0x46
				&& bytes[8] == 0x57
				&& bytes[9] == 0x45
				&& bytes[10] == 0x42
				&& bytes[11] == 0x50) {
			return Optional.of(ImageFileType.WEBP);
		}

		return Optional.empty();
	}

	private boolean startsWith(byte[] bytes, byte[] prefix) {
		if (bytes.length < prefix.length) {
			return false;
		}

		for (int index = 0; index < prefix.length; index += 1) {
			if (bytes[index] != prefix[index]) {
				return false;
			}
		}

		return true;
	}

	private String normalizeOriginalFilename(String originalFilename) {
		if (originalFilename == null || originalFilename.isBlank()) {
			return null;
		}

		String filename = originalFilename.replace('\\', '/');
		int lastSlashIndex = filename.lastIndexOf('/');
		if (lastSlashIndex >= 0) {
			filename = filename.substring(lastSlashIndex + 1);
		}

		filename = filename.trim();
		return filename.isBlank() ? null : filename.toLowerCase(Locale.ROOT);
	}

	private enum ImageFileType {
		JPEG("jpg", "image/jpeg"),
		PNG("png", "image/png"),
		WEBP("webp", "image/webp");

		private final String extension;
		private final String contentType;

		ImageFileType(String extension, String contentType) {
			this.extension = extension;
			this.contentType = contentType;
		}

		private String extension() {
			return extension;
		}

		private String contentType() {
			return contentType;
		}
	}
}
