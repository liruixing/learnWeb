package com.dennis.learn.user

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class User(
	val id: Long,
	val username: String,
	val email: String?,
	val phone: String?,
	val passwordHash: String,
	val nickname: String,
	val avatarUrl: String?,
	val bio: String?,
	val status: UserStatus,
	val lastLoginAt: LocalDateTime?,
	val createdAt: LocalDateTime,
	val updatedAt: LocalDateTime,
	val deletedAt: LocalDateTime?,
)

data class Role(
	val id: Long,
	val code: String,
	val name: String,
	val description: String?,
)

enum class UserStatus {
	active,
	disabled,
}

data class CreateUserRequest(
	@field:Schema(description = "登录用户名，只允许字母、数字和下划线", example = "dennis")
	@field:NotBlank
	@field:Size(min = 3, max = 50)
	@field:Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "username can only contain letters, numbers and underscore")
	val username: String,

	@field:Schema(description = "邮箱", example = "dennis@example.com")
	@field:Email
	@field:Size(max = 100)
	val email: String?,

	@field:Schema(description = "手机号", example = "13800000000")
	@field:Size(max = 30)
	val phone: String?,

	@field:Schema(description = "登录密码，长度 8 到 72 位", example = "Password123")
	@field:NotBlank
	@field:Size(min = 8, max = 72)
	val password: String,

	@field:Schema(description = "前台展示昵称", example = "Dennis")
	@field:NotBlank
	@field:Size(max = 50)
	val nickname: String,

	@field:Schema(description = "头像地址", example = "https://example.com/avatar.png")
	@field:Size(max = 500)
	val avatarUrl: String?,

	@field:Schema(description = "个人简介", example = "持续学习 Web 开发")
	@field:Size(max = 500)
	val bio: String?,
)

data class UpdateUserRequest(
	@field:Schema(description = "邮箱", example = "new-dennis@example.com")
	@field:Size(max = 100)
	@field:Email
	val email: String?,

	@field:Schema(description = "手机号", example = "13900000000")
	@field:Size(max = 30)
	val phone: String?,

	@field:Schema(description = "前台展示昵称", example = "Dennis Li")
	@field:Size(max = 50)
	val nickname: String?,

	@field:Schema(description = "头像地址", example = "https://example.com/new-avatar.png")
	@field:Size(max = 500)
	val avatarUrl: String?,

	@field:Schema(description = "个人简介", example = "产品和后端开发学习者")
	@field:Size(max = 500)
	val bio: String?,
)

data class UpdateUserStatusRequest(
	@field:Schema(description = "用户状态", example = "active")
	val status: UserStatus,
)

data class AssignRolesRequest(
	@field:Schema(description = "角色编码集合", example = "[\"USER\", \"AUTHOR\"]")
	val roleCodes: Set<String>,
)

data class UserResponse(
	val id: Long,
	val username: String,
	val email: String?,
	val phone: String?,
	val nickname: String,
	val avatarUrl: String?,
	val bio: String?,
	val status: UserStatus,
	val roles: List<String>,
	val lastLoginAt: LocalDateTime?,
	val createdAt: LocalDateTime,
	val updatedAt: LocalDateTime,
)

fun User.toResponse(roles: List<Role> = emptyList()): UserResponse =
	UserResponse(
		id = id,
		username = username,
		email = email,
		phone = phone,
		nickname = nickname,
		avatarUrl = avatarUrl,
		bio = bio,
		status = status,
		roles = roles.map { it.code },
		lastLoginAt = lastLoginAt,
		createdAt = createdAt,
		updatedAt = updatedAt,
	)
