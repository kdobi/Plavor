package com.plavor.cart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CartControllerTests {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private MockMvc mockMvc;

	@Test
	void getCartRequiresLogin() throws Exception {
		mockMvc.perform(get("/api/cart"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("AUTH_UNAUTHORIZED"));
	}

	@Test
	void getCartReturnsEmptyCart() throws Exception {
		String accessToken = signupAndLogin("empty-cart@example.com");

		mockMvc.perform(get("/api/cart")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.items", hasSize(0)))
				.andExpect(jsonPath("$.totalQuantity").value(0))
				.andExpect(jsonPath("$.totalAmount").value(0));
	}

	@Test
	void addItemAddsProductToCart() throws Exception {
		String accessToken = signupAndLogin("add-cart@example.com");

		mockMvc.perform(post("/api/cart/items")
						.header("Authorization", "Bearer " + accessToken)
						.contentType("application/json")
						.content("""
								{
								  "productId": 1,
								  "quantity": 2
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.items", hasSize(1)))
				.andExpect(jsonPath("$.items[0].id").isNumber())
				.andExpect(jsonPath("$.items[0].productId").value(1))
				.andExpect(jsonPath("$.items[0].productName").value("Minimal Cotton T-Shirt"))
				.andExpect(jsonPath("$.items[0].unitPrice").value(29000))
				.andExpect(jsonPath("$.items[0].quantity").value(2))
				.andExpect(jsonPath("$.items[0].totalPrice").value(58000))
				.andExpect(jsonPath("$.totalQuantity").value(2))
				.andExpect(jsonPath("$.totalAmount").value(58000));
	}

	@Test
	void addSameProductIncreasesQuantity() throws Exception {
		String accessToken = signupAndLogin("increase-cart@example.com");

		addItem(accessToken, 1, 1);

		mockMvc.perform(post("/api/cart/items")
						.header("Authorization", "Bearer " + accessToken)
						.contentType("application/json")
						.content("""
								{
								  "productId": 1,
								  "quantity": 2
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.items", hasSize(1)))
				.andExpect(jsonPath("$.items[0].quantity").value(3))
				.andExpect(jsonPath("$.totalQuantity").value(3))
				.andExpect(jsonPath("$.totalAmount").value(87000));
	}

	@Test
	void updateItemChangesQuantity() throws Exception {
		String accessToken = signupAndLogin("update-cart@example.com");
		Long itemId = addItem(accessToken, 1, 1);

		mockMvc.perform(patch("/api/cart/items/{itemId}", itemId)
						.header("Authorization", "Bearer " + accessToken)
						.contentType("application/json")
						.content("""
								{
								  "quantity": 4
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items", hasSize(1)))
				.andExpect(jsonPath("$.items[0].id").value(itemId))
				.andExpect(jsonPath("$.items[0].quantity").value(4))
				.andExpect(jsonPath("$.totalQuantity").value(4))
				.andExpect(jsonPath("$.totalAmount").value(116000));
	}

	@Test
	void deleteItemRemovesProductFromCart() throws Exception {
		String accessToken = signupAndLogin("delete-cart@example.com");
		Long itemId = addItem(accessToken, 1, 2);

		mockMvc.perform(delete("/api/cart/items/{itemId}", itemId)
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/cart")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items", hasSize(0)))
				.andExpect(jsonPath("$.totalQuantity").value(0))
				.andExpect(jsonPath("$.totalAmount").value(0));
	}

	@Test
	void addItemRejectsSoldOutProduct() throws Exception {
		String accessToken = signupAndLogin("soldout-cart@example.com");

		mockMvc.perform(post("/api/cart/items")
						.header("Authorization", "Bearer " + accessToken)
						.contentType("application/json")
						.content("""
								{
								  "productId": 3,
								  "quantity": 1
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("CART_PRODUCT_NOT_AVAILABLE"))
				.andExpect(jsonPath("$.message").value("장바구니에 담을 수 없는 상품입니다."));
	}

	@Test
	void addItemRejectsQuantityExceedingStock() throws Exception {
		String accessToken = signupAndLogin("stock-cart@example.com");

		mockMvc.perform(post("/api/cart/items")
						.header("Authorization", "Bearer " + accessToken)
						.contentType("application/json")
						.content("""
								{
								  "productId": 1,
								  "quantity": 121
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("CART_QUANTITY_EXCEEDS_STOCK"))
				.andExpect(jsonPath("$.message").value("상품 재고보다 많은 수량을 담을 수 없습니다."));
	}

	@Test
	void updateItemRejectsOtherMembersCartItem() throws Exception {
		String ownerToken = signupAndLogin("owner-cart@example.com");
		String otherToken = signupAndLogin("other-cart@example.com");
		Long itemId = addItem(ownerToken, 1, 1);

		mockMvc.perform(patch("/api/cart/items/{itemId}", itemId)
						.header("Authorization", "Bearer " + otherToken)
						.contentType("application/json")
						.content("""
								{
								  "quantity": 2
								}
								"""))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("CART_ITEM_NOT_FOUND"))
				.andExpect(jsonPath("$.message").value("장바구니 상품을 찾을 수 없습니다."));
	}

	private Long addItem(String accessToken, long productId, int quantity) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/cart/items")
						.header("Authorization", "Bearer " + accessToken)
						.contentType("application/json")
						.content("""
								{
								  "productId": %d,
								  "quantity": %d
								}
								""".formatted(productId, quantity)))
				.andExpect(status().isCreated())
				.andReturn();

		return objectMapper.readTree(result.getResponse().getContentAsString())
				.get("items")
				.get(0)
				.get("id")
				.asLong();
	}

	private String signupAndLogin(String email) throws Exception {
		mockMvc.perform(post("/api/auth/signup")
						.contentType("application/json")
						.content("""
								{
								  "email": "%s",
								  "password": "password1234",
								  "name": "장바구니 사용자"
								}
								""".formatted(email)))
				.andExpect(status().isCreated());

		MvcResult result = mockMvc.perform(post("/api/auth/login")
						.contentType("application/json")
						.content("""
								{
								  "email": "%s",
								  "password": "password1234"
								}
								""".formatted(email)))
				.andExpect(status().isOk())
				.andReturn();

		return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
	}
}
