package com.dennis.learn.common

import org.springframework.http.HttpStatus

class ApiException(
	val status: HttpStatus,
	override val message: String,
) : RuntimeException(message)

fun notFound(message: String): Nothing = throw ApiException(HttpStatus.NOT_FOUND, message)

fun conflict(message: String): Nothing = throw ApiException(HttpStatus.CONFLICT, message)

fun badRequest(message: String): Nothing = throw ApiException(HttpStatus.BAD_REQUEST, message)
