package com.plavor.global.error;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException exception) {
		ErrorCode errorCode = exception.getErrorCode();

		return ResponseEntity
				.status(errorCode.getStatus())
				.body(ErrorResponse.of(errorCode, exception.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
			MethodArgumentNotValidException exception
	) {
		List<FieldErrorResponse> errors = exception.getBindingResult().getFieldErrors().stream()
				.map(FieldErrorResponse::from)
				.toList();

		return ResponseEntity
				.status(ErrorCode.COMMON_INVALID_INPUT.getStatus())
				.body(ErrorResponse.of(ErrorCode.COMMON_INVALID_INPUT, errors));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException exception) {
		List<FieldErrorResponse> errors = exception.getConstraintViolations().stream()
				.map(violation -> new FieldErrorResponse(
						violation.getPropertyPath().toString(),
						violation.getMessage()
				))
				.toList();

		return ResponseEntity
				.status(ErrorCode.COMMON_INVALID_INPUT.getStatus())
				.body(ErrorResponse.of(ErrorCode.COMMON_INVALID_INPUT, errors));
	}

	@ExceptionHandler({
			HttpMessageNotReadableException.class,
			MethodArgumentTypeMismatchException.class,
			MissingServletRequestParameterException.class
	})
	public ResponseEntity<ErrorResponse> handleInvalidRequestException(Exception exception) {
		return ResponseEntity
				.status(ErrorCode.COMMON_INVALID_INPUT.getStatus())
				.body(ErrorResponse.of(ErrorCode.COMMON_INVALID_INPUT));
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
			HttpRequestMethodNotSupportedException exception
	) {
		return ResponseEntity
				.status(ErrorCode.COMMON_METHOD_NOT_ALLOWED.getStatus())
				.body(ErrorResponse.of(ErrorCode.COMMON_METHOD_NOT_ALLOWED));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception exception) {
		return ResponseEntity
				.status(ErrorCode.COMMON_INTERNAL_SERVER_ERROR.getStatus())
				.body(ErrorResponse.of(ErrorCode.COMMON_INTERNAL_SERVER_ERROR));
	}
}

