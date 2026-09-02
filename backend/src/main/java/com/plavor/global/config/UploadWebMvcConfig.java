package com.plavor.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties(UploadProperties.class)
public class UploadWebMvcConfig implements WebMvcConfigurer {

	private final UploadProperties uploadProperties;

	public UploadWebMvcConfig(UploadProperties uploadProperties) {
		this.uploadProperties = uploadProperties;
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		Path uploadDirectory = uploadProperties.getAbsoluteImageDirectory();
		String resourceLocation = uploadDirectory.toUri().toString();
		if (!resourceLocation.endsWith("/")) {
			resourceLocation = resourceLocation + "/";
		}

		registry.addResourceHandler(uploadProperties.getNormalizedPublicUrlPrefix() + "/**")
				.addResourceLocations(resourceLocation)
				.setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic());
	}
}
