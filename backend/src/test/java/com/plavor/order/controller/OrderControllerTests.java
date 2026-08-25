package com.plavor.order.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.plavor.catalog.domain.Product;
import com.plavor.catalog.domain.ProductStatus;
import com.plavor.catalog.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OrderControllerTests {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ProductRepository productRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Test
	void createOrderRequiresLogin() throws Exception {
		mockMvc.perform(post("/api/orders")
						.contentType("application/json")
						.content("""
								{
								  "cartItemIds": [1],
								  "receiverName": "김동빈",
								  "receiverPhone": "01012345678",
								  "postalCode": "06236",
								  "address": "서울특별시 강남구 테헤란로 123"
								}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("AUTH_UNAUTHORIZED"));
	}

	@Test
	void createOrderCreatesOrderFromSelectedCartItems() throws Exception {
		String accessToken = signupAndLogin("order-create@example.com");
		Long firstItemId = addItem(accessToken, 1, 2);
		Long secondItemId = addItem(accessToken, 2, 1);

		mockMvc.perform(post("/api/orders")
						.header("Authorization", "Bearer " + accessToken)
						.contentType("application/json")
						.content("""
								{
								  "cartItemIds": [%d, %d],
								  "receiverName": "김동빈",
								  "receiverPhone": "01012345678",
								  "postalCode": "06236",
								  "address": "서울특별시 강남구 테헤란로 123",
								  "addressDetail": "10층",
								  "deliveryMessage": "문 앞에 놓아주세요."
								}
								""".formatted(firstItemId, secondItemId)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.orderNumber", startsWith("PLV")))
				.andExpect(jsonPath("$.status").value("CREATED"))
				.andExpect(jsonPath("$.totalAmount").value(127000))
				.andExpect(jsonPath("$.receiverName").value("김동빈"))
				.andExpect(jsonPath("$.receiverPhone").value("01012345678"))
				.andExpect(jsonPath("$.postalCode").value("06236"))
				.andExpect(jsonPath("$.address").value("서울특별시 강남구 테헤란로 123"))
				.andExpect(jsonPath("$.addressDetail").value("10층"))
				.andExpect(jsonPath("$.deliveryMessage").value("문 앞에 놓아주세요."))
				.andExpect(jsonPath("$.orderedAt").exists())
				.andExpect(jsonPath("$.items", hasSize(2)))
				.andExpect(jsonPath("$.items[0].productId").value(1))
				.andExpect(jsonPath("$.items[0].productName").value("Minimal Cotton T-Shirt"))
				.andExpect(jsonPath("$.items[0].unitPrice").value(29000))
				.andExpect(jsonPath("$.items[0].quantity").value(2))
				.andExpect(jsonPath("$.items[0].totalPrice").value(58000))
				.andExpect(jsonPath("$.items[1].productId").value(2))
				.andExpect(jsonPath("$.items[1].productName").value("Relaxed Zip Hoodie"))
				.andExpect(jsonPath("$.items[1].unitPrice").value(69000))
				.andExpect(jsonPath("$.items[1].quantity").value(1))
				.andExpect(jsonPath("$.items[1].totalPrice").value(69000));

		entityManager.flush();
		entityManager.clear();

		assertThat(productRepository.findById(1L).orElseThrow().getStockQuantity()).isEqualTo(118);
		assertThat(productRepository.findById(2L).orElseThrow().getStockQuantity()).isEqualTo(44);

		mockMvc.perform(get("/api/cart")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items", hasSize(0)))
				.andExpect(jsonPath("$.totalQuantity").value(0))
				.andExpect(jsonPath("$.totalAmount").value(0));
	}

	@Test
	void createOrderRejectsWhenStockDropsBelowCartQuantity() throws Exception {
		String accessToken = signupAndLogin("order-stock-shortage@example.com");
		Long itemId = addItem(accessToken, 1, 2);
		Product product = productRepository.findById(1L).orElseThrow();
		product.decreaseStock(product.getStockQuantity() - 1);

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(post("/api/orders")
						.header("Authorization", "Bearer " + accessToken)
						.contentType("application/json")
						.content("""
								{
								  "cartItemIds": [%d],
								  "receiverName": "김동빈",
								  "receiverPhone": "01012345678",
								  "postalCode": "06236",
								  "address": "서울특별시 강남구 테헤란로 123"
								}
								""".formatted(itemId)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("ORDER_QUANTITY_EXCEEDS_STOCK"))
				.andExpect(jsonPath("$.message").value("주문 수량이 상품 재고보다 많습니다."));
	}

	@Test
	void createOrderMarksProductSoldOutWhenStockBecomesZero() throws Exception {
		String accessToken = signupAndLogin("order-stock-sold-out@example.com");
		Product product = productRepository.findById(1L).orElseThrow();
		int stockQuantity = product.getStockQuantity();
		Long itemId = addItem(accessToken, product.getId(), stockQuantity);

		mockMvc.perform(post("/api/orders")
						.header("Authorization", "Bearer " + accessToken)
						.contentType("application/json")
						.content("""
								{
								  "cartItemIds": [%d],
								  "receiverName": "김동빈",
								  "receiverPhone": "01012345678",
								  "postalCode": "06236",
								  "address": "서울특별시 강남구 테헤란로 123"
								}
								""".formatted(itemId)))
				.andExpect(status().isCreated());

		entityManager.flush();
		entityManager.clear();

		Product updatedProduct = productRepository.findById(1L).orElseThrow();

		assertThat(updatedProduct.getStockQuantity()).isZero();
		assertThat(updatedProduct.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);
	}

	@Test
	void createOrderOnlyRemovesSelectedCartItems() throws Exception {
		String accessToken = signupAndLogin("order-selected@example.com");
		Long selectedItemId = addItem(accessToken, 1, 1);
		addItem(accessToken, 2, 1);

		mockMvc.perform(post("/api/orders")
						.header("Authorization", "Bearer " + accessToken)
						.contentType("application/json")
						.content("""
								{
								  "cartItemIds": [%d],
								  "receiverName": "김동빈",
								  "receiverPhone": "01012345678",
								  "postalCode": "06236",
								  "address": "서울특별시 강남구 테헤란로 123"
								}
								""".formatted(selectedItemId)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.items", hasSize(1)))
				.andExpect(jsonPath("$.items[0].productId").value(1))
				.andExpect(jsonPath("$.totalAmount").value(29000));

		mockMvc.perform(get("/api/cart")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items", hasSize(1)))
				.andExpect(jsonPath("$.items[0].productId").value(2));
	}

	@Test
	void getOrdersRequiresLogin() throws Exception {
		mockMvc.perform(get("/api/orders"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("AUTH_UNAUTHORIZED"));
	}

	@Test
	void getOrdersReturnsOnlyCurrentMembersOrders() throws Exception {
		String accessToken = signupAndLogin("order-list@example.com");
		JsonNode firstOrder = createOrderFromProduct(accessToken, 1, 1);
		JsonNode secondOrder = createOrderFromProduct(accessToken, 2, 1);

		String otherToken = signupAndLogin("order-list-other@example.com");
		createOrderFromProduct(otherToken, 1, 1);

		mockMvc.perform(get("/api/orders")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[*].orderNumber", hasItem(firstOrder.get("orderNumber").asText())))
				.andExpect(jsonPath("$[*].orderNumber", hasItem(secondOrder.get("orderNumber").asText())));
	}

	@Test
	void getOrderReturnsCurrentMembersOrder() throws Exception {
		String accessToken = signupAndLogin("order-detail@example.com");
		JsonNode order = createOrderFromProduct(accessToken, 1, 2);

		mockMvc.perform(get("/api/orders/{orderId}", order.get("id").asLong())
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(order.get("id").asLong()))
				.andExpect(jsonPath("$.orderNumber").value(order.get("orderNumber").asText()))
				.andExpect(jsonPath("$.totalAmount").value(58000))
				.andExpect(jsonPath("$.items", hasSize(1)))
				.andExpect(jsonPath("$.items[0].productName").value("Minimal Cotton T-Shirt"))
				.andExpect(jsonPath("$.items[0].quantity").value(2));
	}

	@Test
	void getOrderRejectsOtherMembersOrder() throws Exception {
		String ownerToken = signupAndLogin("order-detail-owner@example.com");
		JsonNode order = createOrderFromProduct(ownerToken, 1, 1);

		String otherToken = signupAndLogin("order-detail-other@example.com");

		mockMvc.perform(get("/api/orders/{orderId}", order.get("id").asLong())
						.header("Authorization", "Bearer " + otherToken))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"))
				.andExpect(jsonPath("$.message").value("주문을 찾을 수 없습니다."));
	}

	@Test
	void createOrderRejectsEmptySelection() throws Exception {
		String accessToken = signupAndLogin("order-empty@example.com");

		mockMvc.perform(post("/api/orders")
						.header("Authorization", "Bearer " + accessToken)
						.contentType("application/json")
						.content("""
								{
								  "cartItemIds": [],
								  "receiverName": "김동빈",
								  "receiverPhone": "01012345678",
								  "postalCode": "06236",
								  "address": "서울특별시 강남구 테헤란로 123"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("COMMON_INVALID_INPUT"));
	}

	@Test
	void createOrderRejectsInvalidReceiverPhone() throws Exception {
		String accessToken = signupAndLogin("order-phone@example.com");
		Long itemId = addItem(accessToken, 1, 1);

		mockMvc.perform(post("/api/orders")
						.header("Authorization", "Bearer " + accessToken)
						.contentType("application/json")
						.content("""
								{
								  "cartItemIds": [%d],
								  "receiverName": "김동빈",
								  "receiverPhone": "010-1234-5678",
								  "postalCode": "06236",
								  "address": "서울특별시 강남구 테헤란로 123"
								}
								""".formatted(itemId)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("COMMON_INVALID_INPUT"))
				.andExpect(jsonPath("$.errors[*].field", hasItem("receiverPhone")))
				.andExpect(jsonPath("$.errors[*].message", hasItem("휴대폰 번호 형식이 올바르지 않습니다.")));
	}

	@Test
	void createOrderRejectsInvalidPostalCode() throws Exception {
		String accessToken = signupAndLogin("order-postal@example.com");
		Long itemId = addItem(accessToken, 1, 1);

		mockMvc.perform(post("/api/orders")
						.header("Authorization", "Bearer " + accessToken)
						.contentType("application/json")
						.content("""
								{
								  "cartItemIds": [%d],
								  "receiverName": "김동빈",
								  "receiverPhone": "01012345678",
								  "postalCode": "1234",
								  "address": "서울특별시 강남구 테헤란로 123"
								}
								""".formatted(itemId)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("COMMON_INVALID_INPUT"))
				.andExpect(jsonPath("$.errors[*].field", hasItem("postalCode")))
				.andExpect(jsonPath("$.errors[*].message", hasItem("우편번호는 5자리 숫자여야 합니다.")));
	}

	@Test
	void createOrderRejectsOtherMembersCartItem() throws Exception {
		String ownerToken = signupAndLogin("order-owner@example.com");
		String otherToken = signupAndLogin("order-other@example.com");
		Long itemId = addItem(ownerToken, 1, 1);

		mockMvc.perform(post("/api/orders")
						.header("Authorization", "Bearer " + otherToken)
						.contentType("application/json")
						.content("""
								{
								  "cartItemIds": [%d],
								  "receiverName": "김동빈",
								  "receiverPhone": "01012345678",
								  "postalCode": "06236",
								  "address": "서울특별시 강남구 테헤란로 123"
								}
								""".formatted(itemId)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("ORDER_CART_ITEM_NOT_FOUND"))
				.andExpect(jsonPath("$.message").value("주문할 장바구니 상품을 찾을 수 없습니다."));
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

		JsonNode items = objectMapper.readTree(result.getResponse().getContentAsString()).get("items");
		for (JsonNode item : items) {
			if (item.get("productId").asLong() == productId) {
				return item.get("id").asLong();
			}
		}

		throw new IllegalStateException("장바구니 상품 ID를 찾을 수 없습니다.");
	}

	private JsonNode createOrderFromProduct(String accessToken, long productId, int quantity) throws Exception {
		Long itemId = addItem(accessToken, productId, quantity);

		MvcResult result = mockMvc.perform(post("/api/orders")
						.header("Authorization", "Bearer " + accessToken)
						.contentType("application/json")
						.content("""
								{
								  "cartItemIds": [%d],
								  "receiverName": "김동빈",
								  "receiverPhone": "01012345678",
								  "postalCode": "06236",
								  "address": "서울특별시 강남구 테헤란로 123"
								}
								""".formatted(itemId)))
				.andExpect(status().isCreated())
				.andReturn();

		return objectMapper.readTree(result.getResponse().getContentAsString());
	}

	private String signupAndLogin(String email) throws Exception {
		mockMvc.perform(post("/api/auth/signup")
						.contentType("application/json")
						.content("""
								{
								  "email": "%s",
								  "password": "password1234",
								  "name": "주문 사용자"
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
