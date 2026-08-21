package com.plavor.global.error;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTests {

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders
				.standaloneSetup(new TestErrorController())
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	@Test
	void handlesBusinessException() throws Exception {
		mockMvc.perform(get("/test/business"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("COMMON_NOT_FOUND"))
				.andExpect(jsonPath("$.message").value("테스트 리소스를 찾을 수 없습니다."))
				.andExpect(jsonPath("$.errors", empty()));
	}

	@Test
	void handlesValidationException() throws Exception {
		mockMvc.perform(post("/test/validation")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "",
								  "quantity": 0
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("COMMON_INVALID_INPUT"))
				.andExpect(jsonPath("$.message").value("요청 값이 올바르지 않습니다."))
				.andExpect(jsonPath("$.errors", hasSize(2)))
				.andExpect(jsonPath("$.errors[*].field", containsInAnyOrder("name", "quantity")));
	}

	@Test
	void handlesUnsupportedMethodException() throws Exception {
		mockMvc.perform(get("/test/validation"))
				.andExpect(status().isMethodNotAllowed())
				.andExpect(jsonPath("$.code").value("COMMON_METHOD_NOT_ALLOWED"))
				.andExpect(jsonPath("$.message").value("허용되지 않은 HTTP 메서드입니다."))
				.andExpect(jsonPath("$.errors", empty()));
	}

	@Test
	void handlesUnexpectedException() throws Exception {
		mockMvc.perform(get("/test/unexpected"))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.code").value("COMMON_INTERNAL_SERVER_ERROR"))
				.andExpect(jsonPath("$.message").value("서버 오류가 발생했습니다."))
				.andExpect(jsonPath("$.errors", empty()));
	}

	@RestController
	private static class TestErrorController {

		@GetMapping("/test/business")
		void business() {
			throw new BusinessException(ErrorCode.COMMON_NOT_FOUND, "테스트 리소스를 찾을 수 없습니다.");
		}

		@PostMapping("/test/validation")
		void validation(@Valid @RequestBody TestRequest request) {
		}

		@GetMapping("/test/unexpected")
		void unexpected() {
			throw new IllegalStateException("unexpected");
		}
	}

	private record TestRequest(
			@NotBlank(message = "이름은 필수입니다.")
			String name,

			@Min(value = 1, message = "수량은 1 이상이어야 합니다.")
			int quantity
	) {
	}
}
