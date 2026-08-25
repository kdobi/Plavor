package com.plavor.admin.controller;

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
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminProductControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Test
	void getProductsRequiresAdmin() throws Exception {
		mockMvc.perform(get("/api/admin/products"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("AUTH_UNAUTHORIZED"));

		mockMvc.perform(get("/api/admin/products")
						.header("Authorization", "Bearer " + createAccessToken("admin-user@example.com", MemberRole.USER)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("AUTH_FORBIDDEN"))
				.andExpect(jsonPath("$.message").value("접근 권한이 없습니다."));
	}

	@Test
	void getProductsReturnsHiddenProductsForAdmin() throws Exception {
		mockMvc.perform(get("/api/admin/products")
						.header("Authorization", "Bearer " + createAdminAccessToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(4)))
				.andExpect(jsonPath("$.content[*].id", contains(4, 3, 2, 1)))
				.andExpect(jsonPath("$.content[0].status").value("HIDDEN"))
				.andExpect(jsonPath("$.content[1].status").value("SOLD_OUT"))
				.andExpect(jsonPath("$.content[2].status").value("ACTIVE"))
				.andExpect(jsonPath("$.totalElements").value(4));
	}

	@Test
	void createProductAddsProductAndImages() throws Exception {
		mockMvc.perform(post("/api/admin/products")
						.header("Authorization", "Bearer " + createAdminAccessToken())
						.contentType("application/json")
						.content("""
								{
								  "categoryId": 1,
								  "name": "Admin Cotton Shirt",
								  "description": "관리자가 등록한 상품입니다.",
								  "price": 41000,
								  "stockQuantity": 8,
								  "status": "ACTIVE",
								  "images": [
								    {
								      "imageUrl": "https://images.unsplash.com/photo-admin-shirt",
								      "altText": "Admin Cotton Shirt",
								      "displayOrder": 1,
								      "thumbnail": false
								    }
								  ]
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.category.id").value(1))
				.andExpect(jsonPath("$.name").value("Admin Cotton Shirt"))
				.andExpect(jsonPath("$.price").value(41000))
				.andExpect(jsonPath("$.stockQuantity").value(8))
				.andExpect(jsonPath("$.status").value("ACTIVE"))
				.andExpect(jsonPath("$.images", hasSize(1)))
				.andExpect(jsonPath("$.images[0].imageUrl").value("https://images.unsplash.com/photo-admin-shirt"))
				.andExpect(jsonPath("$.images[0].thumbnail").value(true));
	}

	@Test
	void updateProductChangesProductAndHidesFromPublicCatalog() throws Exception {
		mockMvc.perform(put("/api/admin/products/1")
						.header("Authorization", "Bearer " + createAdminAccessToken())
						.contentType("application/json")
						.content("""
								{
								  "categoryId": 2,
								  "name": "Updated Hidden Shirt",
								  "description": "관리자가 숨김 처리한 상품입니다.",
								  "price": 51000,
								  "stockQuantity": 7,
								  "status": "HIDDEN",
								  "images": []
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.category.id").value(2))
				.andExpect(jsonPath("$.name").value("Updated Hidden Shirt"))
				.andExpect(jsonPath("$.price").value(51000))
				.andExpect(jsonPath("$.stockQuantity").value(7))
				.andExpect(jsonPath("$.status").value("HIDDEN"))
				.andExpect(jsonPath("$.images", hasSize(0)));

		mockMvc.perform(get("/api/products/1"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("상품을 찾을 수 없습니다."));
	}

	@Test
	void updateStatusRejectsActiveStatusWithoutStock() throws Exception {
		mockMvc.perform(patch("/api/admin/products/3/status")
						.header("Authorization", "Bearer " + createAdminAccessToken())
						.contentType("application/json")
						.content("""
								{
								  "status": "ACTIVE"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("COMMON_INVALID_INPUT"))
				.andExpect(jsonPath("$.message").value("재고가 없는 상품은 판매중으로 변경할 수 없습니다."));
	}

	@Test
	void updateStatusChangesProductStatus() throws Exception {
		mockMvc.perform(patch("/api/admin/products/1/status")
						.header("Authorization", "Bearer " + createAdminAccessToken())
						.contentType("application/json")
						.content("""
								{
								  "status": "HIDDEN"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.status").value("HIDDEN"));
	}

	@Test
	void createProductRejectsMultipleThumbnails() throws Exception {
		mockMvc.perform(post("/api/admin/products")
						.header("Authorization", "Bearer " + createAdminAccessToken())
						.contentType("application/json")
						.content("""
								{
								  "categoryId": 1,
								  "name": "Thumbnail Conflict Shirt",
								  "description": "대표 이미지가 두 개인 잘못된 요청입니다.",
								  "price": 41000,
								  "stockQuantity": 8,
								  "status": "ACTIVE",
								  "images": [
								    {
								      "imageUrl": "https://images.unsplash.com/photo-one",
								      "altText": "첫 번째 이미지",
								      "displayOrder": 1,
								      "thumbnail": true
								    },
								    {
								      "imageUrl": "https://images.unsplash.com/photo-two",
								      "altText": "두 번째 이미지",
								      "displayOrder": 2,
								      "thumbnail": true
								    }
								  ]
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("COMMON_INVALID_INPUT"))
				.andExpect(jsonPath("$.message").value("대표 이미지는 하나만 지정할 수 있습니다."));
	}

	private String createAdminAccessToken() {
		return createAccessToken("admin@example.com", MemberRole.ADMIN);
	}

	private String createAccessToken(String email, MemberRole role) {
		Member member = memberRepository.save(new Member(email, "관리자", null, role));
		return jwtTokenProvider.generateAccessToken(member).value();
	}
}
