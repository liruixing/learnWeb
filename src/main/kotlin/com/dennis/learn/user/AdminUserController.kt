package com.dennis.learn.user

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "后台用户管理", description = "查看用户列表、禁用账号、调整用户角色")
class AdminUserController(
	private val userService: UserService,
) {
	@GetMapping
	@Operation(summary = "后台用户列表", description = "管理员查看用户列表，支持按状态筛选和分页。")
	fun listUsers(
		@RequestParam managerId: Long,
		@RequestParam(required = false) status: UserStatus?,
		@RequestParam(defaultValue = "0") page: Int,
		@RequestParam(defaultValue = "20") size: Int,
	): List<UserResponse> =
		userService.listManagedUsers(managerId, status, page, size)

	@GetMapping("/{id}")
	@Operation(summary = "后台用户详情", description = "管理员查看指定用户资料和角色。")
	fun getUser(
		@PathVariable id: Long,
		@RequestParam managerId: Long,
	): UserResponse =
		userService.getManagedUser(managerId, id)

	@PutMapping("/{id}")
	@Operation(summary = "后台更新用户资料", description = "管理员更新用户邮箱、手机号、昵称、头像和简介。")
	fun updateUser(
		@PathVariable id: Long,
		@RequestParam managerId: Long,
		@Valid @RequestBody request: UpdateUserRequest,
	): UserResponse =
		userService.updateManagedUser(managerId, id, request)

	@PatchMapping("/{id}/status")
	@Operation(summary = "后台更新用户状态", description = "管理员启用或禁用异常账号。")
	fun updateStatus(
		@PathVariable id: Long,
		@RequestBody request: AdminUpdateUserStatusRequest,
	): UserResponse =
		userService.updateManagedStatus(id, request)

	@PutMapping("/{id}/roles")
	@Operation(summary = "后台调整用户角色", description = "管理员替换用户当前角色，支持 USER、AUTHOR、ADMIN。")
	fun assignRoles(
		@PathVariable id: Long,
		@RequestBody request: AdminAssignRolesRequest,
	): UserResponse =
		userService.assignManagedRoles(id, request)

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "后台删除用户", description = "管理员软删除用户，保留历史数据。")
	fun deleteUser(
		@PathVariable id: Long,
		@RequestBody request: AdminDeleteUserRequest,
	) {
		userService.deleteManagedUser(id, request)
	}
}
