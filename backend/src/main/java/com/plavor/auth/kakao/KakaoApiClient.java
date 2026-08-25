package com.plavor.auth.kakao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.plavor.global.error.BusinessException;
import com.plavor.global.error.ErrorCode;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

@Component
public class KakaoApiClient implements KakaoClient {

	private final KakaoProperties kakaoProperties;
	private final HttpClient httpClient = HttpClient.newHttpClient();
	private final ObjectMapper objectMapper = new ObjectMapper();

	public KakaoApiClient(KakaoProperties kakaoProperties) {
		this.kakaoProperties = kakaoProperties;
	}

	@Override
	public String createAuthorizationUrl(String state) {
		Map<String, String> params = new LinkedHashMap<>();
		params.put("response_type", "code");
		params.put("client_id", required(kakaoProperties.getRestApiKey()));
		params.put("redirect_uri", required(kakaoProperties.getRedirectUri()));

		if (state != null && !state.isBlank()) {
			params.put("state", state);
		}

		return kakaoProperties.getAuthorizationUrl() + "?" + formEncode(params);
	}

	@Override
	public KakaoUserInfo fetchUser(String code) {
		String kakaoAccessToken = requestToken(code);
		return requestUserInfo(kakaoAccessToken);
	}

	private String requestToken(String code) {
		Map<String, String> params = new LinkedHashMap<>();
		params.put("grant_type", "authorization_code");
		params.put("client_id", required(kakaoProperties.getRestApiKey()));
		params.put("redirect_uri", required(kakaoProperties.getRedirectUri()));
		params.put("code", code);

		if (kakaoProperties.getClientSecret() != null && !kakaoProperties.getClientSecret().isBlank()) {
			params.put("client_secret", kakaoProperties.getClientSecret());
		}

		HttpRequest request = HttpRequest.newBuilder(URI.create(kakaoProperties.getTokenUrl()))
				.header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
				.POST(HttpRequest.BodyPublishers.ofString(formEncode(params)))
				.build();
		JsonNode response = send(request);
		JsonNode accessToken = response.get("access_token");

		if (accessToken == null || accessToken.asText().isBlank()) {
			throw kakaoLoginFailed();
		}

		return accessToken.asText();
	}

	private KakaoUserInfo requestUserInfo(String kakaoAccessToken) {
		HttpRequest request = HttpRequest.newBuilder(URI.create(kakaoProperties.getUserInfoUrl()))
				.header("Authorization", "Bearer " + kakaoAccessToken)
				.GET()
				.build();
		JsonNode response = send(request);
		JsonNode id = response.get("id");

		if (id == null || id.asText().isBlank()) {
			throw kakaoLoginFailed();
		}

		return new KakaoUserInfo(
				id.asText(),
				readNullableText(response.at("/kakao_account/email")),
				resolveNickname(response)
		);
	}

	private JsonNode send(HttpRequest request) {
		try {
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() < 200 || response.statusCode() >= 300) {
				throw kakaoLoginFailed();
			}

			return objectMapper.readTree(response.body());
		} catch (IOException exception) {
			throw kakaoLoginFailed();
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw kakaoLoginFailed();
		}
	}

	private String resolveNickname(JsonNode response) {
		String nickname = readNullableText(response.at("/kakao_account/profile/nickname"));
		if (nickname == null) {
			nickname = readNullableText(response.at("/properties/nickname"));
		}

		if (nickname == null || nickname.isBlank()) {
			return "카카오 사용자";
		}

		return nickname;
	}

	private String readNullableText(JsonNode node) {
		if (node == null || node.isMissingNode() || node.isNull() || node.asText().isBlank()) {
			return null;
		}

		return node.asText();
	}

	private String formEncode(Map<String, String> params) {
		StringJoiner joiner = new StringJoiner("&");
		params.forEach((key, value) -> joiner.add(encode(key) + "=" + encode(value)));
		return joiner.toString();
	}

	private String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}

	private String required(String value) {
		if (value == null || value.isBlank()) {
			throw kakaoLoginFailed();
		}

		return value;
	}

	private BusinessException kakaoLoginFailed() {
		return new BusinessException(ErrorCode.AUTH_KAKAO_LOGIN_FAILED);
	}
}
