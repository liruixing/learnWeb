package com.dennis.learn.content

import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.ResultSet

@Repository
class ContentRepository(
	private val jdbcClient: JdbcClient,
) {
	fun findPublishedArticles(
		limit: Int,
		offset: Int,
		sort: ArticleSort,
	): List<ArticleSummary> =
		jdbcClient.sql(
			"""
			${baseArticleSummarySql()}
			WHERE a.status = 'published'
			  AND a.deleted_at IS NULL
			  AND a.published_at <= CURRENT_TIMESTAMP
			${orderBy(sort)}
			LIMIT :limit OFFSET :offset
			""".trimIndent(),
		)
			.param("limit", limit)
			.param("offset", offset)
			.query(::mapArticleSummary)
			.list()

	fun findArticleDetail(idOrSlug: String): ArticleDetail? =
		jdbcClient.sql(
			"""
			${baseArticleDetailSql()}
			WHERE a.status = 'published'
			  AND a.deleted_at IS NULL
			  AND a.published_at <= CURRENT_TIMESTAMP
			  AND (a.slug = :idOrSlug OR a.id = :id)
			""".trimIndent(),
		)
			.param("idOrSlug", idOrSlug)
			.param("id", idOrSlug.toLongOrNull() ?: -1)
			.query(::mapArticleDetail)
			.optional()
			.orElse(null)

	fun findArticleDetailById(id: Long): ArticleDetail? =
		jdbcClient.sql(
			"""
			${baseArticleDetailSql()}
			WHERE a.id = :id
			  AND a.deleted_at IS NULL
			""".trimIndent(),
		)
			.param("id", id)
			.query(::mapArticleDetail)
			.optional()
			.orElse(null)

	fun findPublishedArticlesByCategory(
		categorySlug: String,
		limit: Int,
		offset: Int,
		sort: ArticleSort,
	): List<ArticleSummary> =
		jdbcClient.sql(
			"""
			${baseArticleSummarySql()}
			JOIN article_categories ac ON ac.article_id = a.id
			JOIN categories c ON c.id = ac.category_id
			WHERE a.status = 'published'
			  AND a.deleted_at IS NULL
			  AND a.published_at <= CURRENT_TIMESTAMP
			  AND c.deleted_at IS NULL
			  AND c.status = 'active'
			  AND c.slug = :categorySlug
			${orderBy(sort)}
			LIMIT :limit OFFSET :offset
			""".trimIndent(),
		)
			.param("categorySlug", categorySlug)
			.param("limit", limit)
			.param("offset", offset)
			.query(::mapArticleSummary)
			.list()

	fun findPublishedArticlesByTag(
		tagSlug: String,
		limit: Int,
		offset: Int,
		sort: ArticleSort,
	): List<ArticleSummary> =
		jdbcClient.sql(
			"""
			${baseArticleSummarySql()}
			JOIN article_tags at ON at.article_id = a.id
			JOIN tags t ON t.id = at.tag_id
			WHERE a.status = 'published'
			  AND a.deleted_at IS NULL
			  AND a.published_at <= CURRENT_TIMESTAMP
			  AND t.deleted_at IS NULL
			  AND t.status = 'active'
			  AND t.slug = :tagSlug
			${orderBy(sort)}
			LIMIT :limit OFFSET :offset
			""".trimIndent(),
		)
			.param("tagSlug", tagSlug)
			.param("limit", limit)
			.param("offset", offset)
			.query(::mapArticleSummary)
			.list()

	fun searchPublishedArticles(
		keywordPattern: String,
		limit: Int,
		offset: Int,
		sort: ArticleSearchSort,
	): List<ArticleSummary> =
		jdbcClient.sql(
			"""
			${baseArticleSummarySql()}
			WHERE a.status = 'published'
			  AND a.deleted_at IS NULL
			  AND a.published_at <= CURRENT_TIMESTAMP
			  AND (
			    LOWER(a.title) LIKE :keywordPattern ESCAPE '\\'
			    OR LOWER(COALESCE(a.summary, '')) LIKE :keywordPattern ESCAPE '\\'
			    OR LOWER(a.content_md) LIKE :keywordPattern ESCAPE '\\'
			  )
			${searchOrderBy(sort)}
			LIMIT :limit OFFSET :offset
			""".trimIndent(),
		)
			.param("keywordPattern", keywordPattern)
			.param("limit", limit)
			.param("offset", offset)
			.query(::mapArticleSummary)
			.list()

	fun findManagedArticles(
		limit: Int,
		offset: Int,
		keywordPattern: String?,
		status: ArticleStatus?,
		authorId: Long?,
	): List<ArticleSummary> {
		val sql = buildString {
			append(
				"""
				${baseArticleSummarySql()}
				WHERE a.deleted_at IS NULL
				""".trimIndent(),
			)
			if (keywordPattern != null) {
				append(
					"""

					  AND (
					    LOWER(a.title) LIKE :keywordPattern ESCAPE '\\'
					    OR LOWER(COALESCE(a.summary, '')) LIKE :keywordPattern ESCAPE '\\'
					    OR LOWER(a.content_md) LIKE :keywordPattern ESCAPE '\\'
					  )
					""".trimIndent(),
				)
			}
			if (status != null) {
				append("\n  AND a.status = :status")
			}
			if (authorId != null) {
				append("\n  AND a.author_id = :authorId")
			}
			append("\nORDER BY a.updated_at DESC, a.id DESC")
			append("\nLIMIT :limit OFFSET :offset")
		}

		var spec = jdbcClient.sql(sql)
			.param("limit", limit)
			.param("offset", offset)
		if (keywordPattern != null) {
			spec = spec.param("keywordPattern", keywordPattern)
		}
		if (status != null) {
			spec = spec.param("status", status.name)
		}
		if (authorId != null) {
			spec = spec.param("authorId", authorId)
		}

		return spec.query(::mapArticleSummary).list()
	}

	fun findCategories(): List<Category> =
		jdbcClient.sql(
			"""
			SELECT id, name, slug, description, sort_order
			FROM categories
			WHERE status = 'active' AND deleted_at IS NULL
			ORDER BY sort_order ASC, id ASC
			""".trimIndent(),
		)
			.query(::mapCategory)
			.list()

	fun findCategoryById(id: Long): Category? =
		jdbcClient.sql(
			"""
			SELECT id, name, slug, description, sort_order
			FROM categories
			WHERE id = :id AND deleted_at IS NULL
			""".trimIndent(),
		)
			.param("id", id)
			.query(::mapCategory)
			.optional()
			.orElse(null)

	fun findTags(): List<Tag> =
		jdbcClient.sql(
			"""
			SELECT id, name, slug
			FROM tags
			WHERE status = 'active' AND deleted_at IS NULL
			ORDER BY name ASC, id ASC
			""".trimIndent(),
		)
			.query(::mapTag)
			.list()

	fun findTagById(id: Long): Tag? =
		jdbcClient.sql(
			"""
			SELECT id, name, slug
			FROM tags
			WHERE id = :id AND deleted_at IS NULL
			""".trimIndent(),
		)
			.param("id", id)
			.query(::mapTag)
			.optional()
			.orElse(null)

	fun findCategoriesBySlugs(slugs: Set<String>): List<Category> {
		if (slugs.isEmpty()) {
			return emptyList()
		}

		return jdbcClient.sql(
			"""
			SELECT id, name, slug, description, sort_order
			FROM categories
			WHERE status = 'active'
			  AND deleted_at IS NULL
			  AND slug IN (:slugs)
			ORDER BY sort_order ASC, id ASC
			""".trimIndent(),
		)
			.param("slugs", slugs)
			.query(::mapCategory)
			.list()
	}

	fun findTagsBySlugs(slugs: Set<String>): List<Tag> {
		if (slugs.isEmpty()) {
			return emptyList()
		}

		return jdbcClient.sql(
			"""
			SELECT id, name, slug
			FROM tags
			WHERE status = 'active'
			  AND deleted_at IS NULL
			  AND slug IN (:slugs)
			ORDER BY name ASC, id ASC
			""".trimIndent(),
		)
			.param("slugs", slugs)
			.query(::mapTag)
			.list()
	}

	fun findCategoriesByArticleIds(articleIds: Collection<Long>): Map<Long, List<Category>> {
		if (articleIds.isEmpty()) {
			return emptyMap()
		}

		return jdbcClient.sql(
			"""
			SELECT ac.article_id, c.id, c.name, c.slug, c.description, c.sort_order
			FROM article_categories ac
			JOIN categories c ON c.id = ac.category_id
			WHERE ac.article_id IN (:articleIds)
			  AND c.status = 'active'
			  AND c.deleted_at IS NULL
			ORDER BY c.sort_order ASC, c.id ASC
			""".trimIndent(),
		)
			.param("articleIds", articleIds)
			.query { rs, _ -> rs.getLong("article_id") to mapCategory(rs) }
			.list()
			.groupBy({ it.first }, { it.second })
	}

	fun findTagsByArticleIds(articleIds: Collection<Long>): Map<Long, List<Tag>> {
		if (articleIds.isEmpty()) {
			return emptyMap()
		}

		return jdbcClient.sql(
			"""
			SELECT at.article_id, t.id, t.name, t.slug
			FROM article_tags at
			JOIN tags t ON t.id = at.tag_id
			WHERE at.article_id IN (:articleIds)
			  AND t.status = 'active'
			  AND t.deleted_at IS NULL
			ORDER BY t.name ASC, t.id ASC
			""".trimIndent(),
		)
			.param("articleIds", articleIds)
			.query { rs, _ -> rs.getLong("article_id") to mapTag(rs) }
			.list()
			.groupBy({ it.first }, { it.second })
	}

	fun incrementViewCount(articleId: Long) {
		jdbcClient.sql(
			"""
			UPDATE articles
			SET view_count = view_count + 1
			WHERE id = :articleId
			""".trimIndent(),
		)
			.param("articleId", articleId)
			.update()
	}

	fun createDraftArticle(
		request: CreateArticleRequest,
		slug: String,
	): ArticleDetail {
		val keyHolder = GeneratedKeyHolder()

		jdbcClient.sql(
			"""
			INSERT INTO articles (
			  author_id, title, slug, summary, content_md, content_html, cover_url, status
			)
			VALUES (
			  :authorId, :title, :slug, :summary, :contentMd, :contentHtml, :coverUrl, 'draft'
			)
			""".trimIndent(),
		)
			.param("authorId", request.authorId)
			.param("title", request.title.trim())
			.param("slug", slug)
			.param("summary", request.summary?.trim())
			.param("contentMd", request.contentMd)
			.param("contentHtml", request.contentHtml)
			.param("coverUrl", request.coverUrl?.trim())
			.update(keyHolder, "id")

		val id = keyHolder.key?.toLong() ?: error("failed to read generated article id")
		return findArticleDetailById(id) ?: error("created article was not found")
	}

	fun updateArticle(
		id: Long,
		request: UpdateArticleRequest,
		slug: String,
	): ArticleDetail? {
		jdbcClient.sql(
			"""
			UPDATE articles
			SET title = :title,
			    slug = :slug,
			    summary = :summary,
			    content_md = :contentMd,
			    content_html = :contentHtml,
			    cover_url = :coverUrl
			WHERE id = :id
			  AND deleted_at IS NULL
			""".trimIndent(),
		)
			.param("id", id)
			.param("title", request.title.trim())
			.param("slug", slug)
			.param("summary", request.summary?.trim())
			.param("contentMd", request.contentMd)
			.param("contentHtml", request.contentHtml)
			.param("coverUrl", request.coverUrl?.trim())
			.update()

		return findArticleDetailById(id)
	}

	fun updateArticleStatus(id: Long, status: ArticleStatus): ArticleDetail? {
		val publishedAtSql = if (status == ArticleStatus.published) {
			", published_at = COALESCE(published_at, CURRENT_TIMESTAMP)"
		} else {
			""
		}

		jdbcClient.sql(
			"""
			UPDATE articles
			SET status = :status$publishedAtSql
			WHERE id = :id
			  AND deleted_at IS NULL
			""".trimIndent(),
		)
			.param("id", id)
			.param("status", status.name)
			.update()

		return findArticleDetailById(id)
	}

	fun publishArticle(id: Long): ArticleDetail? {
		jdbcClient.sql(
			"""
			UPDATE articles
			SET status = 'published',
			    published_at = COALESCE(published_at, CURRENT_TIMESTAMP)
			WHERE id = :id
			  AND deleted_at IS NULL
			""".trimIndent(),
		)
			.param("id", id)
			.update()

		return findArticleDetailById(id)
	}

	fun softDeleteArticle(id: Long): Boolean =
		jdbcClient.sql(
			"""
			UPDATE articles
			SET slug = CONCAT('deleted-', id),
			    deleted_at = CURRENT_TIMESTAMP
			WHERE id = :id
			  AND deleted_at IS NULL
			""".trimIndent(),
		)
			.param("id", id)
			.update() > 0

	fun existsArticleSlug(slug: String, excludeArticleId: Long? = null): Boolean {
		val sql = buildString {
			append(
				"""
				SELECT COUNT(1)
				FROM articles
				WHERE slug = :slug
				  AND deleted_at IS NULL
				""".trimIndent(),
			)
			if (excludeArticleId != null) {
				append(" AND id <> :excludeArticleId")
			}
		}

		val spec = jdbcClient.sql(sql)
			.param("slug", slug)

		return (excludeArticleId?.let { spec.param("excludeArticleId", it) } ?: spec)
			.query(Int::class.java)
			.single() > 0
	}

	fun userHasAnyRole(userId: Long, roleCodes: Set<String>): Boolean =
		jdbcClient.sql(
			"""
			SELECT COUNT(1)
			FROM users u
			JOIN user_roles ur ON ur.user_id = u.id
			JOIN roles r ON r.id = ur.role_id
			WHERE u.id = :userId
			  AND u.status = 'active'
			  AND u.deleted_at IS NULL
			  AND r.code IN (:roleCodes)
			""".trimIndent(),
		)
			.param("userId", userId)
			.param("roleCodes", roleCodes)
			.query(Int::class.java)
			.single() > 0

	fun replaceArticleCategories(articleId: Long, categories: List<Category>) {
		jdbcClient.sql("DELETE FROM article_categories WHERE article_id = :articleId")
			.param("articleId", articleId)
			.update()

		categories.forEach { category ->
			jdbcClient.sql(
				"""
				INSERT INTO article_categories (article_id, category_id)
				VALUES (:articleId, :categoryId)
				""".trimIndent(),
			)
				.param("articleId", articleId)
				.param("categoryId", category.id)
				.update()
		}
	}

	fun replaceArticleTags(articleId: Long, tags: List<Tag>) {
		jdbcClient.sql("DELETE FROM article_tags WHERE article_id = :articleId")
			.param("articleId", articleId)
			.update()

		tags.forEach { tag ->
			jdbcClient.sql(
				"""
				INSERT INTO article_tags (article_id, tag_id)
				VALUES (:articleId, :tagId)
				""".trimIndent(),
			)
				.param("articleId", articleId)
				.param("tagId", tag.id)
				.update()
		}
	}

	fun createCategory(request: CreateCategoryRequest, slug: String): Category {
		val keyHolder = GeneratedKeyHolder()

		jdbcClient.sql(
			"""
			INSERT INTO categories (name, slug, description, sort_order, status)
			VALUES (:name, :slug, :description, :sortOrder, 'active')
			""".trimIndent(),
		)
			.param("name", request.name.trim())
			.param("slug", slug)
			.param("description", request.description?.trim())
			.param("sortOrder", request.sortOrder)
			.update(keyHolder, "id")

		val id = keyHolder.key?.toLong() ?: error("failed to read generated category id")
		return findCategoryById(id) ?: error("created category was not found")
	}

	fun updateCategory(id: Long, request: UpdateCategoryRequest, slug: String): Category? {
		jdbcClient.sql(
			"""
			UPDATE categories
			SET name = :name,
			    slug = :slug,
			    description = :description,
			    sort_order = :sortOrder,
			    status = 'active'
			WHERE id = :id
			  AND deleted_at IS NULL
			""".trimIndent(),
		)
			.param("id", id)
			.param("name", request.name.trim())
			.param("slug", slug)
			.param("description", request.description?.trim())
			.param("sortOrder", request.sortOrder)
			.update()

		return findCategoryById(id)
	}

	fun softDeleteCategory(id: Long): Boolean =
		jdbcClient.sql(
			"""
			UPDATE categories
			SET name = CONCAT('deleted-', id),
			    slug = CONCAT('deleted-', id),
			    deleted_at = CURRENT_TIMESTAMP,
			    status = 'disabled'
			WHERE id = :id
			  AND deleted_at IS NULL
			""".trimIndent(),
		)
			.param("id", id)
			.update() > 0

	fun existsCategoryName(name: String, excludeCategoryId: Long? = null): Boolean =
		existsOrganizationValue(
			table = "categories",
			column = "name",
			value = name,
			idParamName = "excludeCategoryId",
			excludeId = excludeCategoryId,
		)

	fun existsCategorySlug(slug: String, excludeCategoryId: Long? = null): Boolean =
		existsOrganizationValue(
			table = "categories",
			column = "slug",
			value = slug,
			idParamName = "excludeCategoryId",
			excludeId = excludeCategoryId,
		)

	fun countActiveArticlesByCategoryId(categoryId: Long): Int =
		jdbcClient.sql(
			"""
			SELECT COUNT(1)
			FROM article_categories ac
			JOIN articles a ON a.id = ac.article_id
			WHERE ac.category_id = :categoryId
			  AND a.deleted_at IS NULL
			""".trimIndent(),
		)
			.param("categoryId", categoryId)
			.query(Int::class.java)
			.single()

	fun createTag(request: CreateTagRequest, slug: String): Tag {
		val keyHolder = GeneratedKeyHolder()

		jdbcClient.sql(
			"""
			INSERT INTO tags (name, slug, status)
			VALUES (:name, :slug, 'active')
			""".trimIndent(),
		)
			.param("name", request.name.trim())
			.param("slug", slug)
			.update(keyHolder, "id")

		val id = keyHolder.key?.toLong() ?: error("failed to read generated tag id")
		return findTagById(id) ?: error("created tag was not found")
	}

	fun updateTag(id: Long, request: UpdateTagRequest, slug: String): Tag? {
		jdbcClient.sql(
			"""
			UPDATE tags
			SET name = :name,
			    slug = :slug,
			    status = 'active'
			WHERE id = :id
			  AND deleted_at IS NULL
			""".trimIndent(),
		)
			.param("id", id)
			.param("name", request.name.trim())
			.param("slug", slug)
			.update()

		return findTagById(id)
	}

	fun softDeleteTag(id: Long): Boolean =
		jdbcClient.sql(
			"""
			UPDATE tags
			SET name = CONCAT('deleted-', id),
			    slug = CONCAT('deleted-', id),
			    deleted_at = CURRENT_TIMESTAMP,
			    status = 'disabled'
			WHERE id = :id
			  AND deleted_at IS NULL
			""".trimIndent(),
		)
			.param("id", id)
			.update() > 0

	fun existsTagName(name: String, excludeTagId: Long? = null): Boolean =
		existsOrganizationValue(
			table = "tags",
			column = "name",
			value = name,
			idParamName = "excludeTagId",
			excludeId = excludeTagId,
		)

	fun existsTagSlug(slug: String, excludeTagId: Long? = null): Boolean =
		existsOrganizationValue(
			table = "tags",
			column = "slug",
			value = slug,
			idParamName = "excludeTagId",
			excludeId = excludeTagId,
		)

	fun countActiveArticlesByTagId(tagId: Long): Int =
		jdbcClient.sql(
			"""
			SELECT COUNT(1)
			FROM article_tags at
			JOIN articles a ON a.id = at.article_id
			WHERE at.tag_id = :tagId
			  AND a.deleted_at IS NULL
			""".trimIndent(),
		)
			.param("tagId", tagId)
			.query(Int::class.java)
			.single()

	fun findVisibleCommentsByArticleId(
		articleId: Long,
		limit: Int,
		offset: Int,
	): List<Comment> =
		jdbcClient.sql(
			"""
			${baseCommentSql()}
			WHERE c.article_id = :articleId
			  AND c.status = 'visible'
			  AND c.deleted_at IS NULL
			ORDER BY c.created_at ASC, c.id ASC
			LIMIT :limit OFFSET :offset
			""".trimIndent(),
		)
			.param("articleId", articleId)
			.param("limit", limit)
			.param("offset", offset)
			.query(::mapComment)
			.list()

	fun findManagedComments(
		limit: Int,
		offset: Int,
		articleId: Long?,
		status: CommentStatus?,
		authorId: Long?,
	): List<Comment> {
		val sql = buildString {
			append(
				"""
				${baseCommentSql()}
				WHERE c.deleted_at IS NULL
				""".trimIndent(),
			)
			if (articleId != null) {
				append("\n  AND c.article_id = :articleId")
			}
			if (status != null) {
				append("\n  AND c.status = :status")
			}
			if (authorId != null) {
				append("\n  AND a.author_id = :authorId")
			}
			append("\nORDER BY c.created_at DESC, c.id DESC")
			append("\nLIMIT :limit OFFSET :offset")
		}

		var spec = jdbcClient.sql(sql)
			.param("limit", limit)
			.param("offset", offset)
		if (articleId != null) {
			spec = spec.param("articleId", articleId)
		}
		if (status != null) {
			spec = spec.param("status", status.name)
		}
		if (authorId != null) {
			spec = spec.param("authorId", authorId)
		}

		return spec.query(::mapComment).list()
	}

	fun findCommentById(id: Long): Comment? =
		jdbcClient.sql(
			"""
			${baseCommentSql()}
			WHERE c.id = :id
			  AND c.deleted_at IS NULL
			""".trimIndent(),
		)
			.param("id", id)
			.query(::mapComment)
			.optional()
			.orElse(null)

	fun createComment(articleId: Long, request: CreateCommentRequest): Comment {
		val keyHolder = GeneratedKeyHolder()

		jdbcClient.sql(
			"""
			INSERT INTO comments (article_id, user_id, parent_id, content, status)
			VALUES (:articleId, :userId, :parentId, :content, 'visible')
			""".trimIndent(),
		)
			.param("articleId", articleId)
			.param("userId", request.userId)
			.param("parentId", request.parentId)
			.param("content", request.content.trim())
			.update(keyHolder, "id")

		val id = keyHolder.key?.toLong() ?: error("failed to read generated comment id")
		return findCommentById(id) ?: error("created comment was not found")
	}

	fun updateCommentStatus(id: Long, status: CommentStatus): Comment? {
		jdbcClient.sql(
			"""
			UPDATE comments
			SET status = :status
			WHERE id = :id
			  AND deleted_at IS NULL
			""".trimIndent(),
		)
			.param("id", id)
			.param("status", status.name)
			.update()

		return findCommentById(id)
	}

	fun softDeleteComment(id: Long): Boolean =
		jdbcClient.sql(
			"""
			UPDATE comments
			SET deleted_at = CURRENT_TIMESTAMP
			WHERE id = :id
			  AND deleted_at IS NULL
			""".trimIndent(),
		)
			.param("id", id)
			.update() > 0

	fun incrementArticleCommentCount(articleId: Long) {
		jdbcClient.sql(
			"""
			UPDATE articles
			SET comment_count = comment_count + 1
			WHERE id = :articleId
			""".trimIndent(),
		)
			.param("articleId", articleId)
			.update()
	}

	fun decrementArticleCommentCount(articleId: Long) {
		jdbcClient.sql(
			"""
			UPDATE articles
			SET comment_count = GREATEST(comment_count - 1, 0)
			WHERE id = :articleId
			""".trimIndent(),
		)
			.param("articleId", articleId)
			.update()
	}

	private fun existsOrganizationValue(
		table: String,
		column: String,
		value: String,
		idParamName: String,
		excludeId: Long?,
	): Boolean {
		require(table in setOf("categories", "tags")) { "unsupported organization table: $table" }
		require(column in setOf("name", "slug")) { "unsupported organization column: $column" }

		val sql = buildString {
			append(
				"""
				SELECT COUNT(1)
				FROM $table
				WHERE $column = :value
				  AND deleted_at IS NULL
				""".trimIndent(),
			)
			if (excludeId != null) {
				append(" AND id <> :$idParamName")
			}
		}

		val spec = jdbcClient.sql(sql)
			.param("value", value)

		return (excludeId?.let { spec.param(idParamName, it) } ?: spec)
			.query(Int::class.java)
			.single() > 0
	}

	private fun baseArticleSummarySql(): String =
		"""
		SELECT a.id, a.title, a.slug, a.summary, a.cover_url, a.author_id,
		       u.nickname AS author_name, a.status, a.view_count, a.comment_count,
		       a.published_at, a.created_at, a.updated_at
		FROM articles a
		JOIN users u ON u.id = a.author_id
		""".trimIndent()

	private fun baseArticleDetailSql(): String =
		"""
		SELECT a.id, a.title, a.slug, a.summary, a.content_md, a.content_html,
		       a.cover_url, a.author_id, u.nickname AS author_name, a.status,
		       a.view_count, a.comment_count, a.published_at, a.created_at, a.updated_at
		FROM articles a
		JOIN users u ON u.id = a.author_id
		""".trimIndent()

	private fun baseCommentSql(): String =
		"""
		SELECT c.id, c.article_id, a.title AS article_title, c.user_id,
		       u.nickname AS user_name, c.parent_id, c.content, c.status,
		       c.created_at, c.updated_at
		FROM comments c
		JOIN articles a ON a.id = c.article_id
		JOIN users u ON u.id = c.user_id
		""".trimIndent()

	private fun orderBy(sort: ArticleSort): String =
		when (sort) {
			ArticleSort.latest -> "ORDER BY a.published_at DESC, a.id DESC"
			ArticleSort.hot -> "ORDER BY a.view_count DESC, a.published_at DESC, a.id DESC"
		}

	private fun searchOrderBy(sort: ArticleSearchSort): String =
		when (sort) {
			ArticleSearchSort.relevance ->
				"""
				ORDER BY
				  (
				    CASE WHEN LOWER(a.title) LIKE :keywordPattern ESCAPE '\\' THEN 30 ELSE 0 END +
				    CASE WHEN LOWER(COALESCE(a.summary, '')) LIKE :keywordPattern ESCAPE '\\' THEN 20 ELSE 0 END +
				    CASE WHEN LOWER(a.content_md) LIKE :keywordPattern ESCAPE '\\' THEN 10 ELSE 0 END
				  ) DESC,
				  a.published_at DESC,
				  a.id DESC
				""".trimIndent()
			ArticleSearchSort.latest -> "ORDER BY a.published_at DESC, a.id DESC"
			ArticleSearchSort.hot -> "ORDER BY a.view_count DESC, a.published_at DESC, a.id DESC"
		}

	private fun mapArticleSummary(rs: ResultSet, rowNum: Int): ArticleSummary =
		ArticleSummary(
			id = rs.getLong("id"),
			title = rs.getString("title"),
			slug = rs.getString("slug"),
			summary = rs.getString("summary"),
			coverUrl = rs.getString("cover_url"),
			authorId = rs.getLong("author_id"),
			authorName = rs.getString("author_name"),
			status = ArticleStatus.valueOf(rs.getString("status")),
			viewCount = rs.getLong("view_count"),
			commentCount = rs.getLong("comment_count"),
			publishedAt = rs.getTimestamp("published_at")?.toLocalDateTime(),
			createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
			updatedAt = rs.getTimestamp("updated_at").toLocalDateTime(),
		)

	private fun mapArticleDetail(rs: ResultSet, rowNum: Int): ArticleDetail =
		ArticleDetail(
			id = rs.getLong("id"),
			title = rs.getString("title"),
			slug = rs.getString("slug"),
			summary = rs.getString("summary"),
			contentMd = rs.getString("content_md"),
			contentHtml = rs.getString("content_html"),
			coverUrl = rs.getString("cover_url"),
			authorId = rs.getLong("author_id"),
			authorName = rs.getString("author_name"),
			status = ArticleStatus.valueOf(rs.getString("status")),
			viewCount = rs.getLong("view_count"),
			commentCount = rs.getLong("comment_count"),
			publishedAt = rs.getTimestamp("published_at")?.toLocalDateTime(),
			createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
			updatedAt = rs.getTimestamp("updated_at").toLocalDateTime(),
		)

	private fun mapCategory(rs: ResultSet, rowNum: Int = 0): Category =
		Category(
			id = rs.getLong("id"),
			name = rs.getString("name"),
			slug = rs.getString("slug"),
			description = rs.getString("description"),
			sortOrder = rs.getInt("sort_order"),
		)

	private fun mapTag(rs: ResultSet, rowNum: Int = 0): Tag =
		Tag(
			id = rs.getLong("id"),
			name = rs.getString("name"),
			slug = rs.getString("slug"),
		)

	private fun mapComment(rs: ResultSet, rowNum: Int): Comment =
		Comment(
			id = rs.getLong("id"),
			articleId = rs.getLong("article_id"),
			articleTitle = rs.getString("article_title"),
			userId = rs.getLong("user_id"),
			userName = rs.getString("user_name"),
			parentId = rs.getLong("parent_id").takeUnless { rs.wasNull() },
			content = rs.getString("content"),
			status = CommentStatus.valueOf(rs.getString("status")),
			createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
			updatedAt = rs.getTimestamp("updated_at").toLocalDateTime(),
		)
}
