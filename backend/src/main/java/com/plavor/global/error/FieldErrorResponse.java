package com.plavor.global.error;

import org.springframework.validation.FieldError;

public record FieldErrorResponse(
		String field,
		String message
) {

	public static FieldErrorResponse from(FieldError fieldError) {
		return new FieldErrorResponse(fieldError.getField(), fieldError.getDefaultMessage());
	}
}
