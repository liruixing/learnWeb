package com.dennis.learn.common

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

data class ApiErrorResponse(
	val code: String,
	val message: String,
	val details: Map<String, String> = emptyMap(),
)

@RestControllerAdvice
class GlobalExceptionHandler {
	@ExceptionHandler(ApiException::class)
	fun handleApiException(exception: ApiException): ResponseEntity<ApiErrorResponse> =
		ResponseEntity
			.status(exception.status)
			.body(
				ApiErrorResponse(
					code = exception.status.name,
					message = exception.message,
				),
			)

	@ExceptionHandler(MethodArgumentNotValidException::class)
	fun handleValidationException(exception: MethodArgumentNotValidException): ResponseEntity<ApiErrorResponse> {
		val details = exception.bindingResult
			.allErrors
			.filterIsInstance<FieldError>()
			.associate { it.field to (it.defaultMessage ?: "invalid value") }

		return ResponseEntity
			.status(HttpStatus.BAD_REQUEST)
			.body(
				ApiErrorResponse(
					code = HttpStatus.BAD_REQUEST.name,
					message = "request validation failed",
					details = details,
				),
			)
	}
}
