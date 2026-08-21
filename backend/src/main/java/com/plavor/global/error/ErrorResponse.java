package com.plavor.global.error;

import java.util.List;

public record ErrorResponse(
		String code,
		String message,
		List<FieldErrorResponse> errors
) {

	public static ErrorResponse of(ErrorCode errorCode) {
		return of(errorCode, errorCode.getMessage());
	}

	public static ErrorResponse of(ErrorCode errorCode, String message) {
		return new ErrorResponse(errorCode.getCode(), message, List.of());
	}

	public static ErrorResponse of(ErrorCode errorCode, List<FieldErrorResponse> errors) {
		return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), List.copyOf(errors));
	}
}
