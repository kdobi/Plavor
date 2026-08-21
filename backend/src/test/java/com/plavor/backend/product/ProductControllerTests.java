package com.plavor.backend.product;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void getProductsReturnsPublicProducts() throws Exception {
		mockMvc.perform(get("/api/products"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(3)))
				.andExpect(jsonPath("$[*].slug", containsInAnyOrder(
						"minimal-cotton-t-shirt",
						"relaxed-zip-hoodie",
						"daily-denim-jacket"
				)))
				.andExpect(jsonPath("$[*].slug").value(containsInAnyOrder(
						"minimal-cotton-t-shirt",
						"relaxed-zip-hoodie",
						"daily-denim-jacket"
				)));
	}

	@Test
	void getProductReturnsProductDetail() throws Exception {
		mockMvc.perform(get("/api/products/{id}", 1))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.name").value("Minimal Cotton T-Shirt"))
				.andExpect(jsonPath("$.category.slug").value("tops"))
				.andExpect(jsonPath("$.images", hasSize(1)))
				.andExpect(jsonPath("$.images[0].thumbnail").value(true));
	}

	@Test
	void getProductDoesNotExposeHiddenProducts() throws Exception {
		mockMvc.perform(get("/api/products/{id}", 4))
				.andExpect(status().isNotFound());
	}
}
