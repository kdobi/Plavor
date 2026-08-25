package com.plavor.admin.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.plavor.global.security.JwtTokenProvider;
import com.plavor.member.domain.Member;
import com.plavor.member.domain.MemberRole;
import com.plavor.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminOrderControllerTests {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Test
	void getOrdersRequiresAdmin() throws Exception {
		mockMvc.perform(get("/api/admin/orders"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("AUTH_UNAUTHORIZED"));

		mockMvc.perform(get("/api/admin/orders")
						.header("Authorization", "Bearer " + createAccessToken("admin-order-user@example.com", MemberRole.USER)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("AUTH_FORBIDDEN"));
	}

	@Test
	void getOrdersReturnsAllMembersOrdersForAdmin() throws Exception {
		String firstUserToken = signupAndLogin("admin-order-list-one@example.com");
		JsonNode firstOrder = createOrderFromProduct(firstUserToken, 1, 1);

		String secondUserToken = signupAndLogin("admin-order-list-two@example.com");
		JsonNode secondOrder = createOrderFromProduct(secondUserToken, 2, 1);

		mockMvc.perform(get("/api/admin/orders")
						.header("Authorization", "Bearer " + createAdminAccessToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(2)))
				.andExpect(jsonPath("$.content[*].orderNumber", hasItem(firstOrder.get("orderNumber").asText())))
				.andExpect(jsonPath("$.content[*].orderNumber", hasItem(secondOrder.get("orderNumber").asText())))
				.andExpect(jsonPath("$.content[*].memberEmail", hasItem("admin-order-list-one@example.com")))
				.andExpect(jsonPath("$.content[*].memberEmail", hasItem("admin-order-list-two@example.com")))
				.andExpect(jsonPath("$.totalElements").value(2));
	}

	@Test
	void getOrderReturnsOrderDetailForAdmin() throws Exception {
		String userToken = signupAndLogin("admin-order-detail@example.com");
		JsonNode order = createOrderFromProduct(userToken, 1, 2);

		mockMvc.perform(get("/api/admin/orders/{orderId}", order.get("id").asLong())
						.header("Authorization", "Bearer " + createAdminAccessToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(order.get("id").asLong()))
				.andExpect(jsonPath("$.orderNumber").value(order.get("orderNumber").asText()))
				.andExpect(jsonPath("$.memberEmail").value("admin-order-detail@example.com"))
				.andExpect(jsonPath("$.receiverName").value("김동빈"))
				.andExpect(jsonPath("$.totalAmount").value(58000))
				.andExpect(jsonPath("$.items", hasSize(1)))
				.andExpect(jsonPath("$.items[0].productName").value("Minimal Cotton T-Shirt"))
				.andExpect(jsonPath("$.items[0].quantity").value(2));
	}

	@Test
	void updateStatusChangesOrderStatus() throws Exception {
		String userToken = signupAndLogin("admin-order-status@example.com");
		JsonNode order = createOrderFromProduct(userToken, 1, 1);

		mockMvc.perform(patch("/api/admin/orders/{orderId}/status", order.get("id").asLong())
						.header("Authorization", "Bearer " + createAdminAccessToken())
						.contentType("application/json")
						.content("""
								{
								  "status": "SHIPPED"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(order.get("id").asLong()))
				.andExpect(jsonPath("$.status").value("SHIPPED"))
				.andExpect(jsonPath("$.updatedAt").exists());
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

	private String createAdminAccessToken() {
		return createAccessToken("admin-order@example.com", MemberRole.ADMIN);
	}

	private String createAccessToken(String email, MemberRole role) {
		Member member = memberRepository.save(new Member(email, "관리자", null, role));
		return jwtTokenProvider.generateAccessToken(member).value();
	}
}
