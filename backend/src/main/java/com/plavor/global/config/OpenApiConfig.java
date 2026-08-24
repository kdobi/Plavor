package com.plavor.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("Plavor API")
						.description("Plavor 쇼핑몰 백엔드 API 문서")
						.version("v1"))
				.servers(List.of(new Server()
						.url("/")
						.description("Current server")));
	}
}
