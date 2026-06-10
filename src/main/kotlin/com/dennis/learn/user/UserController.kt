package com.dennis.learn.user

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
@Tag(name = "用户模块", description = "用户注册、查询、资料维护、状态管理和角色分配")
class UserController(
	private val userService: UserService,
) {
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "创建用户", description = "创建一个新用户，并默认分配 USER 角色。")
	fun createUser(@Valid @RequestBody request: CreateUserRequest): UserResponse =
		userService.createUser(request)

	@GetMapping
	@Operation(summary = "查询用户列表", description = "支持按用户状态筛选，并支持分页。")
	fun listUsers(
		@RequestParam(required = false) status: UserStatus?,
		@RequestParam(defaultValue = "0") page: Int,
		@RequestParam(defaultValue = "20") size: Int,
	): List<UserResponse> =
		userService.listUsers(status, page, size)

	@GetMapping("/{id}")
	@Operation(summary = "查询用户详情", description = "根据用户 ID 查询用户资料和角色信息。")
	fun getUser(@PathVariable id: Long): UserResponse =
		userService.getUser(id)

	@PutMapping("/{id}")
	@Operation(summary = "更新用户资料", description = "更新用户邮箱、手机号、昵称、头像和简介。")
	fun updateUser(
		@PathVariable id: Long,
		@Valid @RequestBody request: UpdateUserRequest,
	): UserResponse =
		userService.updateUser(id, request)

	@PatchMapping("/{id}/status")
	@Operation(summary = "更新用户状态", description = "启用或禁用用户。")
	fun updateStatus(
		@PathVariable id: Long,
		@RequestBody request: UpdateUserStatusRequest,
	): UserResponse =
		userService.updateStatus(id, request.status)

	@PutMapping("/{id}/roles")
	@Operation(summary = "分配用户角色", description = "替换用户当前角色，支持 USER、AUTHOR、ADMIN。")
	fun assignRoles(
		@PathVariable id: Long,
		@RequestBody request: AssignRolesRequest,
	): UserResponse =
		userService.assignRoles(id, request)

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "删除用户", description = "软删除用户，保留历史数据。")
	fun deleteUser(@PathVariable id: Long) {
		userService.deleteUser(id)
	}
}
