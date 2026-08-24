package com.plavor.auth.controller;

import com.plavor.member.repository.MemberRepository;
import com.plavor.member.repository.UserCredentialRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private UserCredentialRepository userCredentialRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void signupCreatesMemberAndCredential() throws Exception {
		mockMvc.perform(post("/api/auth/signup")
						.contentType("application/json")
						.content("""
								{
								  "email": "NewUser@Example.com",
								  "password": "password1234",
								  "name": "김동빈",
								  "phone": "01012345678"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.email").value("newuser@example.com"))
				.andExpect(jsonPath("$.name").value("김동빈"))
				.andExpect(jsonPath("$.phone").value("01012345678"))
				.andExpect(jsonPath("$.role").value("USER"))
				.andExpect(jsonPath("$.status").value("ACTIVE"));

		var member = memberRepository.findByEmail("newuser@example.com").orElseThrow();
		var credential = userCredentialRepository.findByMemberId(member.getId()).orElseThrow();

		assertThat(passwordEncoder.matches("password1234", credential.getPasswordHash())).isTrue();
		assertThat(credential.isEmailVerified()).isFalse();
	}

	@Test
	void signupRejectsDuplicatedEmail() throws Exception {
		String requestBody = """
				{
				  "email": "duplicate@example.com",
				  "password": "password1234",
				  "name": "김동빈"
				}
				""";

		mockMvc.perform(post("/api/auth/signup")
						.contentType("application/json")
						.content(requestBody))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/auth/signup")
						.contentType("application/json")
						.content(requestBody))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("AUTH_EMAIL_ALREADY_EXISTS"))
				.andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다."));
	}

	@Test
	void signupValidatesRequestBody() throws Exception {
		mockMvc.perform(post("/api/auth/signup")
						.contentType("application/json")
						.content("""
								{
								  "email": "not-email",
								  "password": "short",
								  "name": ""
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("COMMON_INVALID_INPUT"))
				.andExpect(jsonPath("$.errors[*].field", hasItem("email")))
				.andExpect(jsonPath("$.errors[*].field", hasItem("password")))
				.andExpect(jsonPath("$.errors[*].field", hasItem("name")));
	}
}
