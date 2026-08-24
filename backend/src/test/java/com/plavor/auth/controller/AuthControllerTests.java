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
import static org.hamcrest.Matchers.matchesPattern;
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

	@Test
	void loginReturnsAccessToken() throws Exception {
		mockMvc.perform(post("/api/auth/signup")
						.contentType("application/json")
						.content("""
								{
								  "email": "login@example.com",
								  "password": "password1234",
								  "name": "로그인 사용자"
								}
								"""))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/auth/login")
						.contentType("application/json")
						.content("""
								{
								  "email": "LOGIN@example.com",
								  "password": "password1234"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken", matchesPattern("^[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+$")))
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.expiresIn").value(3600))
				.andExpect(jsonPath("$.user.email").value("login@example.com"))
				.andExpect(jsonPath("$.user.name").value("로그인 사용자"))
				.andExpect(jsonPath("$.user.role").value("USER"));
	}

	@Test
	void loginRejectsMissingEmail() throws Exception {
		mockMvc.perform(post("/api/auth/login")
						.contentType("application/json")
						.content("""
								{
								  "email": "missing@example.com",
								  "password": "password1234"
								}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("AUTH_INVALID_CREDENTIALS"))
				.andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 올바르지 않습니다."));
	}

	@Test
	void loginRejectsWrongPassword() throws Exception {
		mockMvc.perform(post("/api/auth/signup")
						.contentType("application/json")
						.content("""
								{
								  "email": "wrong-password@example.com",
								  "password": "password1234",
								  "name": "로그인 사용자"
								}
								"""))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/auth/login")
						.contentType("application/json")
						.content("""
								{
								  "email": "wrong-password@example.com",
								  "password": "wrong-password"
								}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("AUTH_INVALID_CREDENTIALS"))
				.andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 올바르지 않습니다."));
	}
}
