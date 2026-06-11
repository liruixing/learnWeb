package com.dennis.learn.content

import com.dennis.learn.attachment.AttachmentService
import com.dennis.learn.common.badRequest
import com.dennis.learn.common.conflict
import com.dennis.learn.common.forbidden
import com.dennis.learn.common.notFound
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ContentService(
	private val contentRepository: ContentRepository,
	private val attachmentService: AttachmentService,
) {
	fun listArticles(query: ArticleListQuery): ArticlePageResponse =
		toPage(
			query = query.normalized(),
			articles = contentRepository.findPublishedArticles(
				limit = query.normalized().size,
				offset = query.normalized().offset,
				sort = query.normalized().sort,
			),
		)

	fun getArticle(idOrSlug: String): ArticleDetailResponse {
		val article = contentRepository.findArticleDetail(idOrSlug) ?: notFound("article not found: $idOrSlug")
		contentRepository.incrementViewCount(article.id)
		val categories = contentRepository.findCategoriesByArticleIds(listOf(article.id))[article.id].orEmpty()
		val tags = contentRepository.findTagsByArticleIds(listOf(article.id))[article.id].orEmpty()
		return article.copy(viewCount = article.viewCount + 1).toResponse(categories, tags)
	}

	fun listCategories(): List<CategoryResponse> =
		contentRepository.findCategories().map { it.toResponse() }

	fun listTags(): List<TagResponse> =
		contentRepository.findTags().map { it.toResponse() }

	@Transactional
	fun createCategory(request: CreateCategoryRequest): CategoryResponse {
		validateOrganizationManager(request.managerId)
		val name = request.name.trim()
		validateCategoryName(name)
		val slug = uniqueCategorySlug(request.slug?.trim().orEmpty().ifBlank { name })

		return contentRepository.createCategory(request, slug).toResponse()
	}

	@Transactional
	fun updateCategory(id: Long, request: UpdateCategoryRequest): CategoryResponse {
		contentRepository.findCategoryById(id) ?: notFound("category not found: $id")
		validateOrganizationManager(request.managerId)
		val name = request.name.trim()
		validateCategoryName(name, id)
		val slug = uniqueCategorySlug(
			source = request.slug?.trim().orEmpty().ifBlank { name },
			excludeCategoryId = id,
		)

		return (contentRepository.updateCategory(id, request, slug) ?: notFound("category not found: $id")).toResponse()
	}

	@Transactional
	fun deleteCategory(id: Long, request: DeleteCategoryRequest) {
		contentRepository.findCategoryById(id) ?: notFound("category not found: $id")
		validateOrganizationManager(request.managerId)
		if (contentRepository.countActiveArticlesByCategoryId(id) > 0) {
			conflict("category is still used by articles")
		}
		if (!contentRepository.softDeleteCategory(id)) {
			notFound("category not found: $id")
		}
	}

	@Transactional
	fun createTag(request: CreateTagRequest): TagResponse {
		validateOrganizationManager(request.managerId)
		val name = request.name.trim()
		validateTagName(name)
		val slug = uniqueTagSlug(request.slug?.trim().orEmpty().ifBlank { name })

		return contentRepository.createTag(request, slug).toResponse()
	}

	@Transactional
	fun updateTag(id: Long, request: UpdateTagRequest): TagResponse {
		contentRepository.findTagById(id) ?: notFound("tag not found: $id")
		validateOrganizationManager(request.managerId)
		val name = request.name.trim()
		validateTagName(name, id)
		val slug = uniqueTagSlug(
			source = request.slug?.trim().orEmpty().ifBlank { name },
			excludeTagId = id,
		)

		return (contentRepository.updateTag(id, request, slug) ?: notFound("tag not found: $id")).toResponse()
	}

	@Transactional
	fun deleteTag(id: Long, request: DeleteTagRequest) {
		contentRepository.findTagById(id) ?: notFound("tag not found: $id")
		validateOrganizationManager(request.managerId)
		if (contentRepository.countActiveArticlesByTagId(id) > 0) {
			conflict("tag is still used by articles")
		}
		if (!contentRepository.softDeleteTag(id)) {
			notFound("tag not found: $id")
		}
	}

	fun listArticlesByCategory(
		categorySlug: String,
		query: ArticleListQuery,
	): ArticlePageResponse {
		val normalized = query.normalized()
		return toPage(
			query = normalized,
			articles = contentRepository.findPublishedArticlesByCategory(
				categorySlug = categorySlug,
				limit = normalized.size,
				offset = normalized.offset,
				sort = normalized.sort,
			),
		)
	}

	fun listArticlesByTag(
		tagSlug: String,
		query: ArticleListQuery,
	): ArticlePageResponse {
		val normalized = query.normalized()
		return toPage(
			query = normalized,
			articles = contentRepository.findPublishedArticlesByTag(
				tagSlug = tagSlug,
				limit = normalized.size,
				offset = normalized.offset,
				sort = normalized.sort,
			),
		)
	}

	fun searchArticles(query: ArticleSearchQuery): ArticleSearchResponse {
		val normalized = query.normalized()
		val articles = contentRepository.searchPublishedArticles(
			keywordPattern = normalized.keyword.toLikePattern(),
			limit = normalized.size,
			offset = normalized.offset,
			sort = normalized.sort,
		)
		val page = toPage(
			query = ArticleListQuery(normalized.page, normalized.size, ArticleSort.latest),
			articles = articles,
		)

		return ArticleSearchResponse(
			keyword = normalized.keyword,
			page = page.page,
			size = page.size,
			sort = normalized.sort,
			items = page.items,
		)
	}

	fun listManagedArticles(
		managerId: Long,
		query: AdminArticleListQuery,
	): ArticlePageResponse {
		validateAdmin(managerId)
		val normalized = query.normalized()
		val keywordPattern = normalized.keyword
			?.trim()
			?.takeIf { it.isNotBlank() }
			?.toLikePattern()

		return toPage(
			query = ArticleListQuery(normalized.page, normalized.size, ArticleSort.latest),
			articles = contentRepository.findManagedArticles(
				limit = normalized.size,
				offset = normalized.offset,
				keywordPattern = keywordPattern,
				status = normalized.status,
				authorId = normalized.authorId,
			),
		)
	}

	fun getManagedArticle(managerId: Long, id: Long): ArticleDetailResponse {
		validateAdmin(managerId)
		val article = contentRepository.findArticleDetailById(id) ?: notFound("article not found: $id")
		val categories = contentRepository.findCategoriesByArticleIds(listOf(article.id))[article.id].orEmpty()
		val tags = contentRepository.findTagsByArticleIds(listOf(article.id))[article.id].orEmpty()
		return article.toResponse(categories, tags)
	}

	@Transactional
	fun createDraftArticle(request: CreateArticleRequest): ArticleDetailResponse {
		validateAuthoringUser(request.authorId)
		val normalizedCategorySlugs = request.categorySlugs.normalizedSlugs("categorySlugs")
		val normalizedTagSlugs = request.tagSlugs.normalizedSlugs("tagSlugs")
		validateArticleCategoryRequired(normalizedCategorySlugs)
		val categories = resolveCategories(normalizedCategorySlugs)
		val tags = resolveTags(normalizedTagSlugs)
		val slug = uniqueSlug(request.slug?.trim().orEmpty().ifBlank { request.title })

		val article = contentRepository.createDraftArticle(request, slug)
		contentRepository.replaceArticleCategories(article.id, categories)
		contentRepository.replaceArticleTags(article.id, tags)
		attachmentService.bindArticleAttachments(
			articleId = article.id,
			operatorId = request.authorId,
			contentMd = request.contentMd,
			contentHtml = request.contentHtml,
			coverUrl = request.coverUrl,
		)

		return article.toResponse(categories, tags)
	}

	@Transactional
	fun updateArticle(id: Long, request: UpdateArticleRequest): ArticleDetailResponse {
		val current = contentRepository.findArticleDetailById(id) ?: notFound("article not found: $id")
		validateArticleManager(current, request.authorId)
		val normalizedCategorySlugs = request.categorySlugs.normalizedSlugs("categorySlugs")
		val normalizedTagSlugs = request.tagSlugs.normalizedSlugs("tagSlugs")
		validateArticleCategoryRequired(normalizedCategorySlugs)
		val categories = resolveCategories(normalizedCategorySlugs)
		val tags = resolveTags(normalizedTagSlugs)
		val slug = uniqueSlug(
			source = request.slug?.trim().orEmpty().ifBlank { request.title },
			excludeArticleId = id,
		)

		val article = contentRepository.updateArticle(id, request, slug) ?: notFound("article not found: $id")
		contentRepository.replaceArticleCategories(article.id, categories)
		contentRepository.replaceArticleTags(article.id, tags)
		attachmentService.bindArticleAttachments(
			articleId = article.id,
			operatorId = request.authorId,
			contentMd = request.contentMd,
			contentHtml = request.contentHtml,
			coverUrl = request.coverUrl,
		)

		return article.toResponse(categories, tags)
	}

	@Transactional
	fun updateManagedArticle(id: Long, request: AdminUpdateArticleRequest): ArticleDetailResponse {
		validateAdmin(request.managerId)
		return updateArticle(id, request.toAuthoringRequest())
	}

	@Transactional
	fun publishArticle(id: Long, request: PublishArticleRequest): ArticleDetailResponse {
		val current = contentRepository.findArticleDetailById(id) ?: notFound("article not found: $id")
		validateArticleManager(current, request.authorId)
		if (current.title.isBlank() || current.contentMd.isBlank()) {
			badRequest("article title and contentMd are required before publishing")
		}

		val article = contentRepository.publishArticle(id) ?: notFound("article not found: $id")
		val categories = contentRepository.findCategoriesByArticleIds(listOf(article.id))[article.id].orEmpty()
		if (categories.isEmpty()) {
			badRequest("article must have at least one category before publishing")
		}
		val tags = contentRepository.findTagsByArticleIds(listOf(article.id))[article.id].orEmpty()
		return article.toResponse(categories, tags)
	}

	@Transactional
	fun publishManagedArticle(id: Long, request: ManageArticleRequest): ArticleDetailResponse {
		validateAdmin(request.managerId)
		val current = contentRepository.findArticleDetailById(id) ?: notFound("article not found: $id")
		if (current.title.isBlank() || current.contentMd.isBlank()) {
			badRequest("article title and contentMd are required before publishing")
		}
		val categories = contentRepository.findCategoriesByArticleIds(listOf(current.id))[current.id].orEmpty()
		if (categories.isEmpty()) {
			badRequest("article must have at least one category before publishing")
		}

		val article = contentRepository.updateArticleStatus(id, ArticleStatus.published)
			?: notFound("article not found: $id")
		val tags = contentRepository.findTagsByArticleIds(listOf(article.id))[article.id].orEmpty()
		return article.toResponse(categories, tags)
	}

	@Transactional
	fun offlineManagedArticle(id: Long, request: ManageArticleRequest): ArticleDetailResponse {
		validateAdmin(request.managerId)
		val article = contentRepository.updateArticleStatus(id, ArticleStatus.offline)
			?: notFound("article not found: $id")
		val categories = contentRepository.findCategoriesByArticleIds(listOf(article.id))[article.id].orEmpty()
		val tags = contentRepository.findTagsByArticleIds(listOf(article.id))[article.id].orEmpty()
		return article.toResponse(categories, tags)
	}

	@Transactional
	fun deleteArticle(id: Long, request: DeleteArticleRequest) {
		val current = contentRepository.findArticleDetailById(id) ?: notFound("article not found: $id")
		validateArticleManager(current, request.authorId)
		if (!contentRepository.softDeleteArticle(id)) {
			notFound("article not found: $id")
		}
	}

	@Transactional
	fun deleteManagedArticle(id: Long, request: ManageArticleRequest) {
		validateAdmin(request.managerId)
		contentRepository.findArticleDetailById(id) ?: notFound("article not found: $id")
		if (!contentRepository.softDeleteArticle(id)) {
			notFound("article not found: $id")
		}
	}

	fun listArticleComments(articleId: Long, page: Int, size: Int): CommentPageResponse {
		val article = contentRepository.findArticleDetailById(articleId) ?: notFound("article not found: $articleId")
		validatePublishedArticle(article)
		val normalized = normalizePage(page, size)
		return CommentPageResponse(
			page = normalized.first,
			size = normalized.second,
			items = contentRepository.findVisibleCommentsByArticleId(
				articleId = articleId,
				limit = normalized.second,
				offset = normalized.first * normalized.second,
			).map { it.toResponse() },
		)
	}

	@Transactional
	fun createComment(articleId: Long, request: CreateCommentRequest): CommentResponse {
		val article = contentRepository.findArticleDetailById(articleId) ?: notFound("article not found: $articleId")
		validatePublishedArticle(article)
		validateCommentUser(request.userId)
		validateCommentContent(request.content)
		validateParentComment(articleId, request.parentId)

		val comment = contentRepository.createComment(articleId, request)
		contentRepository.incrementArticleCommentCount(articleId)
		return comment.toResponse()
	}

	fun listManagedComments(
		managerId: Long,
		query: CommentListQuery,
	): CommentPageResponse {
		val normalized = query.normalized()
		if (normalized.articleId != null) {
			val article = contentRepository.findArticleDetailById(normalized.articleId)
				?: notFound("article not found: ${normalized.articleId}")
			validateCommentManager(article, managerId)
		} else {
			validateCommentListManager(managerId)
		}

		val authorFilter = managerId.takeUnless { contentRepository.userHasAnyRole(it, setOf("ADMIN")) }
		return CommentPageResponse(
			page = normalized.page,
			size = normalized.size,
			items = contentRepository.findManagedComments(
				limit = normalized.size,
				offset = normalized.offset,
				articleId = normalized.articleId,
				status = normalized.status,
				authorId = authorFilter,
			).map { it.toResponse() },
		)
	}

	@Transactional
	fun hideComment(id: Long, request: ManageCommentRequest): CommentResponse {
		val comment = contentRepository.findCommentById(id) ?: notFound("comment not found: $id")
		validateCommentManager(comment, request.managerId)
		if (comment.status == CommentStatus.visible) {
			contentRepository.decrementArticleCommentCount(comment.articleId)
		}
		return (contentRepository.updateCommentStatus(id, CommentStatus.hidden) ?: notFound("comment not found: $id")).toResponse()
	}

	@Transactional
	fun showComment(id: Long, request: ManageCommentRequest): CommentResponse {
		val comment = contentRepository.findCommentById(id) ?: notFound("comment not found: $id")
		validateCommentManager(comment, request.managerId)
		if (comment.status == CommentStatus.hidden) {
			contentRepository.incrementArticleCommentCount(comment.articleId)
		}
		return (contentRepository.updateCommentStatus(id, CommentStatus.visible) ?: notFound("comment not found: $id")).toResponse()
	}

	@Transactional
	fun deleteComment(id: Long, request: ManageCommentRequest) {
		val comment = contentRepository.findCommentById(id) ?: notFound("comment not found: $id")
		validateCommentManager(comment, request.managerId)
		if (comment.status == CommentStatus.visible) {
			contentRepository.decrementArticleCommentCount(comment.articleId)
		}
		if (!contentRepository.softDeleteComment(id)) {
			notFound("comment not found: $id")
		}
	}

	private fun toPage(
		query: ArticleListQuery,
		articles: List<ArticleSummary>,
	): ArticlePageResponse {
		val articleIds = articles.map { it.id }
		val categoriesByArticleId = contentRepository.findCategoriesByArticleIds(articleIds)
		val tagsByArticleId = contentRepository.findTagsByArticleIds(articleIds)

		return ArticlePageResponse(
			page = query.page,
			size = query.size,
			items = articles.map { article ->
				article.toListItem(
					categories = categoriesByArticleId[article.id].orEmpty(),
					tags = tagsByArticleId[article.id].orEmpty(),
				)
			},
		)
	}

	private val ArticleListQuery.offset: Int
		get() = page * size

	private fun ArticleListQuery.normalized(): ArticleListQuery =
		copy(
			page = page.coerceAtLeast(0),
			size = size.coerceIn(1, 100),
		)

	private val ArticleSearchQuery.offset: Int
		get() = page * size

	private val AdminArticleListQuery.offset: Int
		get() = page * size

	private val CommentListQuery.offset: Int
		get() = page * size

	private fun ArticleSearchQuery.normalized(): ArticleSearchQuery {
		val normalizedKeyword = keyword.trim()
		if (normalizedKeyword.isBlank()) {
			badRequest("keyword cannot be blank")
		}
		if (normalizedKeyword.length > 100) {
			badRequest("keyword cannot exceed 100 characters")
		}

		return copy(
			keyword = normalizedKeyword,
			page = page.coerceAtLeast(0),
			size = size.coerceIn(1, 100),
		)
	}

	private fun AdminArticleListQuery.normalized(): AdminArticleListQuery {
		val normalizedKeyword = keyword?.trim()?.takeIf { it.isNotBlank() }
		if (normalizedKeyword != null && normalizedKeyword.length > 100) {
			badRequest("keyword cannot exceed 100 characters")
		}

		return copy(
			page = page.coerceAtLeast(0),
			size = size.coerceIn(1, 100),
			keyword = normalizedKeyword,
		)
	}

	private fun CommentListQuery.normalized(): CommentListQuery =
		copy(
			page = page.coerceAtLeast(0),
			size = size.coerceIn(1, 100),
		)

	private fun normalizePage(page: Int, size: Int): Pair<Int, Int> =
		page.coerceAtLeast(0) to size.coerceIn(1, 100)

	private fun String.toLikePattern(): String =
		"%${lowercase().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")}%"

	private fun validateAuthoringUser(userId: Long) {
		if (!contentRepository.userHasAnyRole(userId, setOf("AUTHOR", "ADMIN"))) {
			forbidden("user is not allowed to create or manage articles")
		}
	}

	private fun validateOrganizationManager(userId: Long) {
		if (!contentRepository.userHasAnyRole(userId, setOf("ADMIN"))) {
			forbidden("only admin can manage categories and tags")
		}
	}

	private fun validateAdmin(userId: Long) {
		if (!contentRepository.userHasAnyRole(userId, setOf("ADMIN"))) {
			forbidden("only admin can access backend management")
		}
	}

	private fun validateCommentUser(userId: Long) {
		if (!contentRepository.userHasAnyRole(userId, setOf("USER", "AUTHOR", "ADMIN"))) {
			forbidden("user is not allowed to comment")
		}
	}

	private fun validateArticleManager(article: ArticleDetail, userId: Long) {
		validateAuthoringUser(userId)
		if (article.authorId == userId) {
			return
		}
		if (!contentRepository.userHasAnyRole(userId, setOf("ADMIN"))) {
			forbidden("only the article author or admin can manage this article")
		}
	}

	private fun validateCommentListManager(userId: Long) {
		if (!contentRepository.userHasAnyRole(userId, setOf("AUTHOR", "ADMIN"))) {
			forbidden("only article authors or admin can manage comments")
		}
	}

	private fun validateCommentManager(comment: Comment, userId: Long) {
		val article = contentRepository.findArticleDetailById(comment.articleId)
			?: notFound("article not found: ${comment.articleId}")
		validateCommentManager(article, userId)
	}

	private fun validateCommentManager(article: ArticleDetail, userId: Long) {
		validateCommentListManager(userId)
		if (article.authorId == userId) {
			return
		}
		if (!contentRepository.userHasAnyRole(userId, setOf("ADMIN"))) {
			forbidden("only the article author or admin can manage this comment")
		}
	}

	private fun validatePublishedArticle(article: ArticleDetail) {
		if (
			article.status != ArticleStatus.published ||
			article.publishedAt == null ||
			article.publishedAt.isAfter(LocalDateTime.now())
		) {
			notFound("article not found: ${article.id}")
		}
	}

	private fun validateCommentContent(content: String) {
		val normalizedContent = content.trim()
		if (normalizedContent.isBlank()) {
			badRequest("comment content cannot be blank")
		}
		if (normalizedContent.length > 1000) {
			badRequest("comment content cannot exceed 1000 characters")
		}
	}

	private fun validateParentComment(articleId: Long, parentId: Long?) {
		if (parentId == null) {
			return
		}
		val parent = contentRepository.findCommentById(parentId) ?: badRequest("parent comment not found: $parentId")
		if (parent.articleId != articleId) {
			badRequest("parent comment does not belong to this article")
		}
		if (parent.status != CommentStatus.visible) {
			badRequest("parent comment is not visible")
		}
	}

	private fun resolveCategories(slugs: Set<String>): List<Category> {
		val categories = contentRepository.findCategoriesBySlugs(slugs)
		val foundSlugs = categories.map { it.slug }.toSet()
		val missingSlugs = slugs - foundSlugs
		if (missingSlugs.isNotEmpty()) {
			badRequest("unknown category slugs: ${missingSlugs.joinToString(", ")}")
		}
		return categories
	}

	private fun resolveTags(slugs: Set<String>): List<Tag> {
		val tags = contentRepository.findTagsBySlugs(slugs)
		val foundSlugs = tags.map { it.slug }.toSet()
		val missingSlugs = slugs - foundSlugs
		if (missingSlugs.isNotEmpty()) {
			badRequest("unknown tag slugs: ${missingSlugs.joinToString(", ")}")
		}
		return tags
	}

	private fun Set<String>.normalizedSlugs(fieldName: String): Set<String> {
		if (size > 20) {
			badRequest("$fieldName cannot contain more than 20 items")
		}

		return map { it.trim().lowercase() }
			.filter { it.isNotBlank() }
			.onEach {
				if (!SLUG_PATTERN.matches(it)) {
					badRequest("$fieldName contains invalid slug: $it")
				}
			}
			.toSet()
	}

	private fun validateArticleCategoryRequired(categorySlugs: Set<String>) {
		if (categorySlugs.isEmpty()) {
			badRequest("article must have at least one category")
		}
	}

	private fun validateCategoryName(name: String, excludeCategoryId: Long? = null) {
		if (name.isBlank()) {
			badRequest("category name cannot be blank")
		}
		if (contentRepository.existsCategoryName(name, excludeCategoryId)) {
			conflict("category name already exists")
		}
	}

	private fun validateTagName(name: String, excludeTagId: Long? = null) {
		if (name.isBlank()) {
			badRequest("tag name cannot be blank")
		}
		if (contentRepository.existsTagName(name, excludeTagId)) {
			conflict("tag name already exists")
		}
	}

	private fun uniqueSlug(source: String, excludeArticleId: Long? = null): String {
		val baseSlug = source.toSlug()
		var candidate = baseSlug
		var suffix = 2
		while (contentRepository.existsArticleSlug(candidate, excludeArticleId)) {
			candidate = "$baseSlug-$suffix"
			suffix += 1
			if (candidate.length > 220) {
				conflict("failed to generate a unique article slug")
			}
		}
		return candidate
	}

	private fun String.toSlug(): String {
		val slug = trim()
			.lowercase()
			.replace(Regex("[^a-z0-9]+"), "-")
			.trim('-')
			.take(220)
			.trim('-')
		return slug.ifBlank { "article" }
	}

	private fun uniqueCategorySlug(source: String, excludeCategoryId: Long? = null): String =
		uniqueOrganizationSlug(
			source = source,
			fallback = "category",
			maxLength = 80,
			exists = { candidate -> contentRepository.existsCategorySlug(candidate, excludeCategoryId) },
		)

	private fun uniqueTagSlug(source: String, excludeTagId: Long? = null): String =
		uniqueOrganizationSlug(
			source = source,
			fallback = "tag",
			maxLength = 80,
			exists = { candidate -> contentRepository.existsTagSlug(candidate, excludeTagId) },
		)

	private fun uniqueOrganizationSlug(
		source: String,
		fallback: String,
		maxLength: Int,
		exists: (String) -> Boolean,
	): String {
		val baseSlug = source.toSlug(fallback).take(maxLength).trim('-').ifBlank { fallback }
		var candidate = baseSlug
		var suffix = 2
		while (exists(candidate)) {
			val suffixText = "-$suffix"
			candidate = "${baseSlug.take(maxLength - suffixText.length).trim('-')}$suffixText"
			suffix += 1
			if (suffix > 1000) {
				conflict("failed to generate a unique slug")
			}
		}
		return candidate
	}

	private fun String.toSlug(fallback: String): String {
		val slug = trim()
			.lowercase()
			.replace(Regex("[^a-z0-9]+"), "-")
			.trim('-')
		return slug.ifBlank { fallback }
	}

	private companion object {
		private val SLUG_PATTERN = Regex("^[a-z0-9][a-z0-9-]{0,218}[a-z0-9]$|^[a-z0-9]$")
	}
}
