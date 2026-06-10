package com.dennis.learn.user

import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.sql.ResultSet

@Repository
class RoleRepository(
	private val jdbcClient: JdbcClient,
) {
	fun findByUserId(userId: Long): List<Role> =
		jdbcClient.sql(
			"""
			SELECT r.id, r.code, r.name, r.description
			FROM roles r
			JOIN user_roles ur ON ur.role_id = r.id
			WHERE ur.user_id = :userId
			ORDER BY r.id ASC
			""".trimIndent(),
		)
			.param("userId", userId)
			.query(::mapRole)
			.list()

	fun findByCodes(codes: Set<String>): List<Role> {
		if (codes.isEmpty()) {
			return emptyList()
		}

		return jdbcClient.sql(
			"""
			SELECT id, code, name, description
			FROM roles
			WHERE code IN (:codes)
			ORDER BY id ASC
			""".trimIndent(),
		)
			.param("codes", codes)
			.query(::mapRole)
			.list()
	}

	fun replaceUserRoles(userId: Long, roles: List<Role>) {
		jdbcClient.sql("DELETE FROM user_roles WHERE user_id = :userId")
			.param("userId", userId)
			.update()

		roles.forEach { role ->
			jdbcClient.sql(
				"""
				INSERT INTO user_roles (user_id, role_id)
				VALUES (:userId, :roleId)
				""".trimIndent(),
			)
				.param("userId", userId)
				.param("roleId", role.id)
				.update()
		}
	}

	private fun mapRole(rs: ResultSet, rowNum: Int): Role =
		Role(
			id = rs.getLong("id"),
			code = rs.getString("code"),
			name = rs.getString("name"),
			description = rs.getString("description"),
		)
}
