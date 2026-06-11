package com.dennis.learn.user

import com.dennis.learn.common.badRequest
import com.dennis.learn.common.conflict
import com.dennis.learn.common.forbidden
import com.dennis.learn.common.notFound
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
	private val userRepository: UserRepository,
	private val roleRepository: RoleRepository,
) {
	private val passwordEncoder = BCryptPasswordEncoder()

	@Transactional
	fun createUser(request: CreateUserRequest): UserResponse {
		validateUniqueCreateFields(request)

		val user = userRepository.create(
			request = request,
			passwordHash = passwordEncoder.encode(request.password)
				?: error("failed to encode password"),
		)
		val userRole = roleRepository.findByCodes(setOf("USER"))
		roleRepository.replaceUserRoles(user.id, userRole)

		return user.toResponse(userRole)
	}

	fun listUsers(status: UserStatus?, page: Int, size: Int): List<UserResponse> {
		val normalizedPage = page.coerceAtLeast(0)
		val normalizedSize = size.coerceIn(1, 100)
		val users = userRepository.findPage(
			status = status,
			limit = normalizedSize,
			offset = normalizedPage * normalizedSize,
		)

		return users.map { user ->
			user.toResponse(roleRepository.findByUserId(user.id))
		}
	}

	fun getUser(id: Long): UserResponse {
		val user = userRepository.findById(id) ?: notFound("user not found: $id")
		return user.toResponse(roleRepository.findByUserId(user.id))
	}

	fun listManagedUsers(managerId: Long, status: UserStatus?, page: Int, size: Int): List<UserResponse> {
		validateAdmin(managerId)
		return listUsers(status, page, size)
	}

	fun getManagedUser(managerId: Long, id: Long): UserResponse {
		validateAdmin(managerId)
		return getUser(id)
	}

	@Transactional
	fun updateUser(id: Long, request: UpdateUserRequest): UserResponse {
		val current = userRepository.findById(id) ?: notFound("user not found: $id")
		validateUniqueUpdateFields(current, request)

		val user = userRepository.update(id, request) ?: notFound("user not found: $id")
		return user.toResponse(roleRepository.findByUserId(user.id))
	}

	@Transactional
	fun updateManagedUser(managerId: Long, id: Long, request: UpdateUserRequest): UserResponse {
		validateAdmin(managerId)
		return updateUser(id, request)
	}

	@Transactional
	fun updateStatus(id: Long, status: UserStatus): UserResponse {
		val user = userRepository.updateStatus(id, status) ?: notFound("user not found: $id")
		return user.toResponse(roleRepository.findByUserId(user.id))
	}

	@Transactional
	fun updateManagedStatus(id: Long, request: AdminUpdateUserStatusRequest): UserResponse {
		validateAdmin(request.managerId)
		if (id == request.managerId && request.status == UserStatus.disabled) {
			badRequest("admin cannot disable current manager account")
		}
		return updateStatus(id, request.status)
	}

	@Transactional
	fun assignRoles(id: Long, request: AssignRolesRequest): UserResponse {
		val user = userRepository.findById(id) ?: notFound("user not found: $id")
		if (request.roleCodes.isEmpty()) {
			badRequest("roleCodes cannot be empty")
		}

		val normalizedCodes = request.roleCodes.map { it.trim().uppercase() }.toSet()
		val roles = roleRepository.findByCodes(normalizedCodes)
		val foundCodes = roles.map { it.code }.toSet()
		val missingCodes = normalizedCodes - foundCodes
		if (missingCodes.isNotEmpty()) {
			badRequest("unknown role codes: ${missingCodes.joinToString(", ")}")
		}

		roleRepository.replaceUserRoles(user.id, roles)
		return user.toResponse(roles)
	}

	@Transactional
	fun assignManagedRoles(id: Long, request: AdminAssignRolesRequest): UserResponse {
		validateAdmin(request.managerId)
		if (id == request.managerId && "ADMIN" !in request.roleCodes.map { it.trim().uppercase() }.toSet()) {
			badRequest("admin cannot remove ADMIN role from current manager account")
		}
		return assignRoles(id, AssignRolesRequest(request.roleCodes))
	}

	@Transactional
	fun deleteUser(id: Long) {
		if (!userRepository.softDelete(id)) {
			notFound("user not found: $id")
		}
	}

	@Transactional
	fun deleteManagedUser(id: Long, request: AdminDeleteUserRequest) {
		validateAdmin(request.managerId)
		if (id == request.managerId) {
			badRequest("admin cannot delete current manager account")
		}
		deleteUser(id)
	}

	fun verifyPassword(username: String, rawPassword: String): Boolean {
		val user = userRepository.findByUsername(username) ?: return false
		return user.status == UserStatus.active && passwordEncoder.matches(rawPassword, user.passwordHash)
	}

	private fun validateUniqueCreateFields(request: CreateUserRequest) {
		if (userRepository.existsByUsername(request.username)) {
			conflict("username already exists")
		}
		request.email?.takeIf { it.isNotBlank() }?.let {
			if (userRepository.existsByEmail(it)) {
				conflict("email already exists")
			}
		}
		request.phone?.takeIf { it.isNotBlank() }?.let {
			if (userRepository.existsByPhone(it)) {
				conflict("phone already exists")
			}
		}
	}

	private fun validateUniqueUpdateFields(current: User, request: UpdateUserRequest) {
		request.email?.takeIf { it.isNotBlank() && it != current.email }?.let {
			if (userRepository.existsByEmail(it)) {
				conflict("email already exists")
			}
		}
		request.phone?.takeIf { it.isNotBlank() && it != current.phone }?.let {
			if (userRepository.existsByPhone(it)) {
				conflict("phone already exists")
			}
		}
	}

	private fun validateAdmin(userId: Long) {
		val user = userRepository.findById(userId) ?: forbidden("only admin can manage users")
		if (user.status != UserStatus.active) {
			forbidden("only active admin can manage users")
		}
		if (roleRepository.findByUserId(userId).none { it.code == "ADMIN" }) {
			forbidden("only admin can manage users")
		}
	}
}
