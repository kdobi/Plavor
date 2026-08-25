package com.plavor.auth.controller;

import com.plavor.auth.kakao.KakaoClient;
import com.plavor.auth.kakao.KakaoUserInfo;
import com.plavor.member.domain.SocialProvider;
import com.plavor.member.repository.MemberRepository;
import com.plavor.member.repository.SocialAccountRepository;
import com.plavor.member.repository.UserCredentialRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTests {

	private static final String KAKAO_OAUTH_STATE_COOKIE_NAME = "PLAVOR_KAKAO_OAUTH_STATE";
	private static final String KAKAO_OAUTH_STATE = "test-oauth-state";
	private static final String REFRESH_TOKEN_COOKIE_NAME = "PLAVOR_REFRESH_TOKEN";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private UserCredentialRepository userCredentialRepository;

	@Autowired
	private SocialAccountRepository socialAccountRepository;

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

		MvcResult result = mockMvc.perform(post("/api/auth/login")
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
				.andExpect(jsonPath("$.user.role").value("USER"))
				.andReturn();

		assertRefreshCookieIssued(result);
	}

	@Test
	void refreshReturnsNewAccessTokenAndRotatesRefreshToken() throws Exception {
		Cookie refreshTokenCookie = loginAndGetRefreshTokenCookie("refresh@example.com");

		MvcResult result = mockMvc.perform(post("/api/auth/refresh")
						.cookie(refreshTokenCookie))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken", matchesPattern("^[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+$")))
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.expiresIn").value(3600))
				.andExpect(jsonPath("$.user.email").value("refresh@example.com"))
				.andReturn();

		Cookie rotatedRefreshTokenCookie = getRefreshTokenCookie(result);

		assertRefreshCookieIssued(result);
		assertThat(rotatedRefreshTokenCookie.getValue()).isNotEqualTo(refreshTokenCookie.getValue());

		mockMvc.perform(post("/api/auth/refresh")
						.cookie(refreshTokenCookie))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("AUTH_INVALID_REFRESH_TOKEN"))
				.andExpect(jsonPath("$.message").value("로그인 세션이 만료되었습니다."));
	}

	@Test
	void refreshRejectsMissingCookie() throws Exception {
		mockMvc.perform(post("/api/auth/refresh"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("AUTH_INVALID_REFRESH_TOKEN"))
				.andExpect(jsonPath("$.message").value("로그인 세션이 만료되었습니다."));
	}

	@Test
	void logoutRevokesRefreshTokenAndExpiresCookie() throws Exception {
		Cookie refreshTokenCookie = loginAndGetRefreshTokenCookie("logout@example.com");

		MvcResult result = mockMvc.perform(post("/api/auth/logout")
						.cookie(refreshTokenCookie))
				.andExpect(status().isNoContent())
				.andReturn();

		assertRefreshCookieExpired(result);

		mockMvc.perform(post("/api/auth/refresh")
						.cookie(refreshTokenCookie))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("AUTH_INVALID_REFRESH_TOKEN"))
				.andExpect(jsonPath("$.message").value("로그인 세션이 만료되었습니다."));
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

	@Test
	void getKakaoLoginUrlReturnsAuthorizationUrl() throws Exception {
		MvcResult result = mockMvc.perform(get("/api/auth/kakao/login-url"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.authorizationUrl", containsString("https://kauth.kakao.com/oauth/authorize?state=")))
				.andReturn();

		Cookie stateCookie = result.getResponse().getCookie(KAKAO_OAUTH_STATE_COOKIE_NAME);

		assertThat(stateCookie).isNotNull();
		assertThat(stateCookie.getValue()).isNotBlank();
		assertThat(result.getResponse().getContentAsString()).contains("state=" + stateCookie.getValue());
	}

	@Test
	void kakaoLoginCreatesMemberAndSocialAccount() throws Exception {
		MvcResult result = mockMvc.perform(post("/api/auth/kakao/login")
						.cookie(new Cookie(KAKAO_OAUTH_STATE_COOKIE_NAME, KAKAO_OAUTH_STATE))
						.contentType("application/json")
						.content("""
								{
								  "code": "valid-kakao-code",
								  "state": "test-oauth-state"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken", matchesPattern("^[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+$")))
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.expiresIn").value(3600))
				.andExpect(jsonPath("$.user.email").value("kakao_3948571029@social.plavor.local"))
				.andExpect(jsonPath("$.user.name").value("카카오 회원"))
				.andExpect(jsonPath("$.user.role").value("USER"))
				.andExpect(jsonPath("$.user.status").value("ACTIVE"))
				.andReturn();

		assertRefreshCookieIssued(result);

		var socialAccount = socialAccountRepository
				.findByProviderAndProviderUserId(SocialProvider.KAKAO, "3948571029")
				.orElseThrow();

		assertThat(socialAccount.getProviderEmail()).isNull();
		assertThat(socialAccount.getMember().getEmail()).isEqualTo("kakao_3948571029@social.plavor.local");
		assertThat(userCredentialRepository.findByMemberId(socialAccount.getMember().getId())).isEmpty();
	}

	@Test
	void kakaoLoginReusesExistingSocialAccount() throws Exception {
		String requestBody = """
				{
				  "code": "valid-kakao-code",
				  "state": "test-oauth-state"
				}
				""";

		mockMvc.perform(post("/api/auth/kakao/login")
						.cookie(new Cookie(KAKAO_OAUTH_STATE_COOKIE_NAME, KAKAO_OAUTH_STATE))
						.contentType("application/json")
						.content(requestBody))
				.andExpect(status().isOk());
		mockMvc.perform(post("/api/auth/kakao/login")
						.cookie(new Cookie(KAKAO_OAUTH_STATE_COOKIE_NAME, KAKAO_OAUTH_STATE))
						.contentType("application/json")
						.content(requestBody))
				.andExpect(status().isOk());

		var member = memberRepository.findByEmail("kakao_3948571029@social.plavor.local").orElseThrow();
		var socialAccount = socialAccountRepository
				.findByProviderAndProviderUserId(SocialProvider.KAKAO, "3948571029")
				.orElseThrow();

		assertThat(socialAccount.getMember().getId()).isEqualTo(member.getId());
	}

	@Test
	void kakaoLoginRejectsInvalidOAuthState() throws Exception {
		mockMvc.perform(post("/api/auth/kakao/login")
						.cookie(new Cookie(KAKAO_OAUTH_STATE_COOKIE_NAME, "different-state"))
						.contentType("application/json")
						.content("""
								{
								  "code": "valid-kakao-code",
								  "state": "test-oauth-state"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("AUTH_INVALID_OAUTH_STATE"))
				.andExpect(jsonPath("$.message").value("소셜 로그인 요청이 만료되었거나 올바르지 않습니다."));
	}

	private Cookie loginAndGetRefreshTokenCookie(String email) throws Exception {
		mockMvc.perform(post("/api/auth/signup")
						.contentType("application/json")
						.content("""
								{
								  "email": "%s",
								  "password": "password1234",
								  "name": "로그인 사용자"
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
								""".formatted(email.toUpperCase())))
				.andExpect(status().isOk())
				.andReturn();

		return getRefreshTokenCookie(result);
	}

	private Cookie getRefreshTokenCookie(MvcResult result) {
		Cookie refreshTokenCookie = result.getResponse().getCookie(REFRESH_TOKEN_COOKIE_NAME);

		assertThat(refreshTokenCookie).isNotNull();
		assertThat(refreshTokenCookie.getValue()).isNotBlank();

		return refreshTokenCookie;
	}

	private void assertRefreshCookieIssued(MvcResult result) {
		assertThat(result.getResponse().getHeaders(HttpHeaders.SET_COOKIE))
				.anySatisfy(header -> assertThat(header)
						.contains(REFRESH_TOKEN_COOKIE_NAME + "=")
						.contains("Path=/api/auth")
						.contains("Max-Age=1209600")
						.contains("HttpOnly")
						.contains("SameSite=Lax"));
	}

	private void assertRefreshCookieExpired(MvcResult result) {
		assertThat(result.getResponse().getHeaders(HttpHeaders.SET_COOKIE))
				.anySatisfy(header -> assertThat(header)
						.contains(REFRESH_TOKEN_COOKIE_NAME + "=")
						.contains("Path=/api/auth")
						.contains("Max-Age=0")
						.contains("HttpOnly")
						.contains("SameSite=Lax"));
	}

	@TestConfiguration
	static class KakaoClientTestConfiguration {

		@Bean
		@Primary
		KakaoClient kakaoClient() {
			return new KakaoClient() {
				@Override
				public String createAuthorizationUrl(String state) {
					return "https://kauth.kakao.com/oauth/authorize?state=" + state;
				}

				@Override
				public KakaoUserInfo fetchUser(String code) {
					return new KakaoUserInfo("3948571029", null, "카카오 회원");
				}
			};
		}
	}
}
