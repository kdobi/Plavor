package com.plavor.catalog.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CatalogControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void getProductsReturnsOnlyPublicProducts() throws Exception {
		mockMvc.perform(get("/api/products"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(3)))
				.andExpect(jsonPath("$.content[*].id", contains(3, 2, 1)))
				.andExpect(jsonPath("$.content[0].status").value("SOLD_OUT"))
				.andExpect(jsonPath("$.content[1].thumbnailImage.imageUrl")
						.value("https://images.unsplash.com/photo-1556821840-3a63f95609a7"))
				.andExpect(jsonPath("$.page").value(0))
				.andExpect(jsonPath("$.size").value(20))
				.andExpect(jsonPath("$.totalElements").value(3));
	}

	@Test
	void getProductsFiltersByCategoryId() throws Exception {
		mockMvc.perform(get("/api/products")
						.param("categoryId", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(1)))
				.andExpect(jsonPath("$.content[0].id").value(1))
				.andExpect(jsonPath("$.content[0].categoryName").value("Tops"));
	}

	@Test
	void getProductsFiltersByKeyword() throws Exception {
		mockMvc.perform(get("/api/products")
						.param("keyword", "hoodie"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(1)))
				.andExpect(jsonPath("$.content[0].id").value(2))
				.andExpect(jsonPath("$.content[0].name").value("Relaxed Zip Hoodie"));
	}

	@Test
	void getProductReturnsProductDetail() throws Exception {
		mockMvc.perform(get("/api/products/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.category.name").value("Tops"))
				.andExpect(jsonPath("$.name").value("Minimal Cotton T-Shirt"))
				.andExpect(jsonPath("$.price").value(29000))
				.andExpect(jsonPath("$.images", hasSize(1)))
				.andExpect(jsonPath("$.images[0].thumbnail").value(true));
	}

	@Test
	void getProductDoesNotExposeHiddenProduct() throws Exception {
		mockMvc.perform(get("/api/products/4"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("COMMON_NOT_FOUND"))
				.andExpect(jsonPath("$.message").value("상품을 찾을 수 없습니다."));
	}

	@Test
	void getCategoriesReturnsActiveCategories() throws Exception {
		mockMvc.perform(get("/api/categories"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(3)))
				.andExpect(jsonPath("$[*].id", contains(1, 2, 3)))
				.andExpect(jsonPath("$[0].name").value("Tops"))
				.andExpect(jsonPath("$[0].slug").value("tops"));
	}
}
