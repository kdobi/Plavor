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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminUploadControllerTests {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Test
	void uploadImageRequiresAdmin() throws Exception {
		mockMvc.perform(multipart("/api/admin/uploads/images").file(pngFile()))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("AUTH_UNAUTHORIZED"));

		mockMvc.perform(multipart("/api/admin/uploads/images")
						.file(pngFile())
						.header("Authorization", "Bearer " + createAccessToken("upload-user@example.com", MemberRole.USER)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("AUTH_FORBIDDEN"));
	}

	@Test
	void uploadImageStoresFileAndReturnsPublicUrl() throws Exception {
		MockMultipartFile file = pngFile();

		MvcResult result = mockMvc.perform(multipart("/api/admin/uploads/images")
						.file(file)
						.header("Authorization", "Bearer " + createAdminAccessToken()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.imageUrl", startsWith("/uploads/products/")))
				.andExpect(jsonPath("$.imageUrl", endsWith(".png")))
				.andExpect(jsonPath("$.originalFilename").value("hoodie.png"))
				.andExpect(jsonPath("$.contentType").value("image/png"))
				.andExpect(jsonPath("$.size").value(file.getSize()))
				.andReturn();

		JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());

		mockMvc.perform(get(response.get("imageUrl").asText()))
				.andExpect(status().isOk())
				.andExpect(content().bytes(file.getBytes()));
	}

	@Test
	void uploadImageRejectsNonImageFile() throws Exception {
		MockMultipartFile textFile = new MockMultipartFile(
				"file",
				"memo.txt",
				"text/plain",
				"not-image".getBytes()
		);

		mockMvc.perform(multipart("/api/admin/uploads/images")
						.file(textFile)
						.header("Authorization", "Bearer " + createAdminAccessToken()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("UPLOAD_INVALID_IMAGE_TYPE"))
				.andExpect(jsonPath("$.message").value("jpg, png, webp 이미지만 업로드할 수 있습니다."));
	}

	private MockMultipartFile pngFile() {
		return new MockMultipartFile(
				"file",
				"hoodie.png",
				"image/png",
				new byte[] {
						(byte) 0x89, 0x50, 0x4E, 0x47,
						0x0D, 0x0A, 0x1A, 0x0A,
						0x00, 0x00, 0x00, 0x0D
				}
		);
	}

	private String createAdminAccessToken() {
		return createAccessToken("upload-admin@example.com", MemberRole.ADMIN);
	}

	private String createAccessToken(String email, MemberRole role) {
		Member member = memberRepository.save(new Member(email, "관리자", null, role));
		return jwtTokenProvider.generateAccessToken(member).value();
	}
}
