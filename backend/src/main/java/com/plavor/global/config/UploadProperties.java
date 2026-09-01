package com.plavor.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties(prefix = "app.upload")
public class UploadProperties {

	private Path imageDirectory = Path.of("./uploads");
	private String publicUrlPrefix = "/uploads";
	private long maxImageSizeBytes = 5L * 1024L * 1024L;

	public Path getImageDirectory() {
		return imageDirectory;
	}

	public void setImageDirectory(Path imageDirectory) {
		this.imageDirectory = imageDirectory;
	}

	public String getPublicUrlPrefix() {
		return publicUrlPrefix;
	}

	public void setPublicUrlPrefix(String publicUrlPrefix) {
		this.publicUrlPrefix = publicUrlPrefix;
	}

	public long getMaxImageSizeBytes() {
		return maxImageSizeBytes;
	}

	public void setMaxImageSizeBytes(long maxImageSizeBytes) {
		this.maxImageSizeBytes = maxImageSizeBytes;
	}

	public Path getAbsoluteImageDirectory() {
		Path directory = imageDirectory == null ? Path.of("./uploads") : imageDirectory;

		return directory.toAbsolutePath().normalize();
	}

	public String getNormalizedPublicUrlPrefix() {
		String prefix = publicUrlPrefix == null || publicUrlPrefix.isBlank()
				? "/uploads"
				: publicUrlPrefix.trim();

		if (!prefix.startsWith("/")) {
			prefix = "/" + prefix;
		}

		while (prefix.length() > 1 && prefix.endsWith("/")) {
			prefix = prefix.substring(0, prefix.length() - 1);
		}

		return prefix;
	}
}
