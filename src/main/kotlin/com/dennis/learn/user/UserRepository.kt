package com.dennis.learn.user

import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.ResultSet

@Repository
class UserRepository(
	private val jdbcClient: JdbcClient,
) {
	fun create(request: CreateUserRequest, passwordHash: String): User {
		val keyHolder = GeneratedKeyHolder()

		jdbcClient.sql(
			"""
			INSERT INTO users (username, email, phone, password_hash, nickname, avatar_url, bio)
			VALUES (:username, :email, :phone, :passwordHash, :nickname, :avatarUrl, :bio)
			""".trimIndent(),
		)
			.param("username", request.username)
			.param("email", request.email)
			.param("phone", request.phone)
			.param("passwordHash", passwordHash)
			.param("nickname", request.nickname)
			.param("avatarUrl", request.avatarUrl)
			.param("bio", request.bio)
			.update(keyHolder, "id")

		val id = keyHolder.key?.toLong() ?: error("failed to read generated user id")
		return findById(id) ?: error("created user was not found")
	}

	fun findById(id: Long): User? =
		jdbcClient.sql(
			"""
			SELECT id, username, email, phone, password_hash, nickname, avatar_url, bio,
			       status, last_login_at, created_at, updated_at, deleted_at
			FROM users
			WHERE id = :id AND deleted_at IS NULL
			""".trimIndent(),
		)
			.param("id", id)
			.query(::mapUser)
			.optional()
			.orElse(null)

	fun findByUsername(username: String): User? =
		jdbcClient.sql(
			"""
			SELECT id, username, email, phone, password_hash, nickname, avatar_url, bio,
			       status, last_login_at, created_at, updated_at, deleted_at
			FROM users
			WHERE username = :username AND deleted_at IS NULL
			""".trimIndent(),
		)
			.param("username", username)
			.query(::mapUser)
			.optional()
			.orElse(null)

	fun existsByUsername(username: String): Boolean =
		existsBy("username", username)

	fun existsByEmail(email: String): Boolean =
		existsBy("email", email)

	fun existsByPhone(phone: String): Boolean =
		existsBy("phone", phone)

	fun findPage(status: UserStatus?, limit: Int, offset: Int): List<User> {
		val sql = buildString {
			append(
				"""
				SELECT id, username, email, phone, password_hash, nickname, avatar_url, bio,
				       status, last_login_at, created_at, updated_at, deleted_at
				FROM users
				WHERE deleted_at IS NULL
				""".trimIndent(),
			)
			if (status != null) {
				append(" AND status = :status")
			}
			append(" ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
		}

		val spec = jdbcClient.sql(sql)
			.param("limit", limit)
			.param("offset", offset)

		return (if (status == null) spec else spec.param("status", status.name))
			.query(::mapUser)
			.list()
	}

	fun update(id: Long, request: UpdateUserRequest): User? {
		jdbcClient.sql(
			"""
			UPDATE users
			SET email = COALESCE(:email, email),
			    phone = COALESCE(:phone, phone),
			    nickname = COALESCE(:nickname, nickname),
			    avatar_url = COALESCE(:avatarUrl, avatar_url),
			    bio = COALESCE(:bio, bio)
			WHERE id = :id AND deleted_at IS NULL
			""".trimIndent(),
		)
			.param("id", id)
			.param("email", request.email)
			.param("phone", request.phone)
			.param("nickname", request.nickname)
			.param("avatarUrl", request.avatarUrl)
			.param("bio", request.bio)
			.update()

		return findById(id)
	}

	fun updateStatus(id: Long, status: UserStatus): User? {
		jdbcClient.sql(
			"""
			UPDATE users
			SET status = :status
			WHERE id = :id AND deleted_at IS NULL
			""".trimIndent(),
		)
			.param("id", id)
			.param("status", status.name)
			.update()

		return findById(id)
	}

	fun softDelete(id: Long): Boolean =
		jdbcClient.sql(
			"""
			UPDATE users
			SET deleted_at = CURRENT_TIMESTAMP
			WHERE id = :id AND deleted_at IS NULL
			""".trimIndent(),
		)
			.param("id", id)
			.update() > 0

	private fun existsBy(column: String, value: String): Boolean {
		val allowedColumns = setOf("username", "email", "phone")
		require(column in allowedColumns) { "unsupported user lookup column: $column" }

		return jdbcClient.sql(
			"""
			SELECT COUNT(1)
			FROM users
			WHERE $column = :value AND deleted_at IS NULL
			""".trimIndent(),
		)
			.param("value", value)
			.query(Int::class.java)
			.single() > 0
	}

	private fun mapUser(rs: ResultSet, rowNum: Int): User =
		User(
			id = rs.getLong("id"),
			username = rs.getString("username"),
			email = rs.getString("email"),
			phone = rs.getString("phone"),
			passwordHash = rs.getString("password_hash"),
			nickname = rs.getString("nickname"),
			avatarUrl = rs.getString("avatar_url"),
			bio = rs.getString("bio"),
			status = UserStatus.valueOf(rs.getString("status")),
			lastLoginAt = rs.getTimestamp("last_login_at")?.toLocalDateTime(),
			createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
			updatedAt = rs.getTimestamp("updated_at").toLocalDateTime(),
			deletedAt = rs.getTimestamp("deleted_at")?.toLocalDateTime(),
		)
}
