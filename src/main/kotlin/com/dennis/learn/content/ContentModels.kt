package com.dennis.learn.content

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class ArticleSummary(
	val id: Long,
	val title: String,
	val slug: String?,
	val summary: String?,
	val coverUrl: String?,
	val authorId: Long,
	val authorName: String,
	val status: ArticleStatus,
	val viewCount: Long,
	val commentCount: Long,
	val publishedAt: LocalDateTime?,
	val createdAt: LocalDateTime,
	val updatedAt: LocalDateTime,
)

data class ArticleDetail(
	val id: Long,
	val title: String,
	val slug: String?,
	val summary: String?,
	val contentMd: String,
	val contentHtml: String?,
	val coverUrl: String?,
	val authorId: Long,
	val authorName: String,
	val status: ArticleStatus,
	val viewCount: Long,
	val commentCount: Long,
	val publishedAt: LocalDateTime?,
	val createdAt: LocalDateTime,
	val updatedAt: LocalDateTime,
)

data class Category(
	val id: Long,
	val name: String,
	val slug: String,
	val description: String?,
	val sortOrder: Int,
)

data class Tag(
	val id: Long,
	val name: String,
	val slug: String,
)

data class Comment(
	val id: Long,
	val articleId: Long,
	val articleTitle: String,
	val userId: Long,
	val userName: String,
	val parentId: Long?,
	val content: String,
	val status: CommentStatus,
	val createdAt: LocalDateTime,
	val updatedAt: LocalDateTime,
)

enum class ArticleStatus {
	draft,
	published,
	offline,
}

enum class CommentStatus {
	visible,
	hidden,
}

data class ArticleListItemResponse(
	val id: Long,
	val title: String,
	val slug: String?,
	val summary: String?,
	val coverUrl: String?,
	val author: AuthorResponse,
	val categories: List<CategoryResponse>,
	val tags: List<TagResponse>,
	val status: ArticleStatus,
	val viewCount: Long,
	val commentCount: Long,
	val publishedAt: LocalDateTime?,
	val createdAt: LocalDateTime,
)

data class ArticleDetailResponse(
	val id: Long,
	val title: String,
	val slug: String?,
	val summary: String?,
	val contentMd: String,
	val contentHtml: String?,
	val coverUrl: String?,
	val author: AuthorResponse,
	val categories: List<CategoryResponse>,
	val tags: List<TagResponse>,
	val status: ArticleStatus,
	val viewCount: Long,
	val commentCount: Long,
	val publishedAt: LocalDateTime?,
	val createdAt: LocalDateTime,
	val updatedAt: LocalDateTime,
)

data class AuthorResponse(
	val id: Long,
	val name: String,
)

data class CategoryResponse(
	val id: Long,
	val name: String,
	val slug: String,
	val description: String?,
)

data class TagResponse(
	val id: Long,
	val name: String,
	val slug: String,
)

data class CommentResponse(
	val id: Long,
	val articleId: Long,
	val articleTitle: String,
	val user: AuthorResponse,
	val parentId: Long?,
	val content: String,
	val status: CommentStatus,
	val createdAt: LocalDateTime,
	val updatedAt: LocalDateTime,
)

data class CommentPageResponse(
	val page: Int,
	val size: Int,
	val items: List<CommentResponse>,
)

data class CreateCommentRequest(
	@field:Schema(description = "评论用户 ID。后续接入登录后可替换为当前登录用户。", example = "2")
	val userId: Long,

	@field:Schema(description = "父评论 ID。一级评论不传，回复评论时传目标评论 ID。", example = "1")
	val parentId: Long?,

	@field:Schema(description = "评论内容", example = "这篇文章讲得很清楚。")
	@field:NotBlank
	@field:Size(max = 1000)
	val content: String,
)

data class ManageCommentRequest(
	@field:Schema(description = "管理用户 ID。文章作者或管理员可管理评论。", example = "2")
	val managerId: Long,
)

data class CommentListQuery(
	@field:Schema(description = "页码，从 0 开始", example = "0")
	val page: Int,

	@field:Schema(description = "每页条数，范围 1 到 100", example = "20")
	val size: Int,

	@field:Schema(description = "按文章 ID 筛选。不传则查询全部。", example = "1")
	val articleId: Long?,

	@field:Schema(description = "按评论状态筛选。不传则查询全部。", example = "visible")
	val status: CommentStatus?,
)

data class CreateCategoryRequest(
	@field:Schema(description = "管理员用户 ID。后续接入登录后可替换为当前登录用户。", example = "2")
	val managerId: Long,

	@field:Schema(description = "分类名称", example = "读书")
	@field:NotBlank
	@field:Size(max = 50)
	val name: String,

	@field:Schema(description = "分类 slug，不传则由名称自动生成", example = "reading")
	@field:Size(max = 80)
	val slug: String?,

	@field:Schema(description = "分类描述", example = "读书笔记、书评和知识整理")
	@field:Size(max = 255)
	val description: String?,

	@field:Schema(description = "排序值，越小越靠前", example = "40")
	val sortOrder: Int = 0,
)

data class UpdateCategoryRequest(
	@field:Schema(description = "管理员用户 ID。后续接入登录后可替换为当前登录用户。", example = "2")
	val managerId: Long,

	@field:Schema(description = "分类名称", example = "读书")
	@field:NotBlank
	@field:Size(max = 50)
	val name: String,

	@field:Schema(description = "分类 slug，不传则由名称自动生成", example = "reading")
	@field:Size(max = 80)
	val slug: String?,

	@field:Schema(description = "分类描述", example = "读书笔记、书评和知识整理")
	@field:Size(max = 255)
	val description: String?,

	@field:Schema(description = "排序值，越小越靠前", example = "40")
	val sortOrder: Int = 0,
)

data class DeleteCategoryRequest(
	@field:Schema(description = "管理员用户 ID。后续接入登录后可替换为当前登录用户。", example = "2")
	val managerId: Long,
)

data class CreateTagRequest(
	@field:Schema(description = "管理员用户 ID。后续接入登录后可替换为当前登录用户。", example = "2")
	val managerId: Long,

	@field:Schema(description = "标签名称", example = "架构")
	@field:NotBlank
	@field:Size(max = 50)
	val name: String,

	@field:Schema(description = "标签 slug，不传则由名称自动生成", example = "architecture")
	@field:Size(max = 80)
	val slug: String?,
)

data class UpdateTagRequest(
	@field:Schema(description = "管理员用户 ID。后续接入登录后可替换为当前登录用户。", example = "2")
	val managerId: Long,

	@field:Schema(description = "标签名称", example = "架构")
	@field:NotBlank
	@field:Size(max = 50)
	val name: String,

	@field:Schema(description = "标签 slug，不传则由名称自动生成", example = "architecture")
	@field:Size(max = 80)
	val slug: String?,
)

data class DeleteTagRequest(
	@field:Schema(description = "管理员用户 ID。后续接入登录后可替换为当前登录用户。", example = "2")
	val managerId: Long,
)

data class ArticleArchiveResponse(
	val year: Int,
	val month: Int,
	val count: Long,
)

data class ArticlePageResponse(
	val page: Int,
	val size: Int,
	val items: List<ArticleListItemResponse>,
)

data class ArticleSearchResponse(
	val keyword: String,
	val page: Int,
	val size: Int,
	val sort: ArticleSearchSort,
	val items: List<ArticleListItemResponse>,
)

enum class ArticleSort {
	latest,
	hot,
}

enum class ArticleSearchSort {
	relevance,
	latest,
	hot,
}

data class ArticleListQuery(
	@field:Schema(description = "页码，从 0 开始", example = "0")
	val page: Int,

	@field:Schema(description = "每页条数，范围 1 到 100", example = "20")
	val size: Int,

	@field:Schema(description = "排序方式：latest 最新，hot 热门", example = "latest")
	val sort: ArticleSort,
)

data class ArticleSearchQuery(
	@field:Schema(description = "搜索关键词", example = "Spring Boot")
	val keyword: String,

	@field:Schema(description = "页码，从 0 开始", example = "0")
	val page: Int,

	@field:Schema(description = "每页条数，范围 1 到 100", example = "20")
	val size: Int,

	@field:Schema(description = "排序方式：relevance 相关度，latest 最新，hot 热门", example = "relevance")
	val sort: ArticleSearchSort,
)

data class AdminArticleListQuery(
	@field:Schema(description = "页码，从 0 开始", example = "0")
	val page: Int,

	@field:Schema(description = "每页条数，范围 1 到 100", example = "20")
	val size: Int,

	@field:Schema(description = "按关键词筛选标题、摘要和正文。不传则查询全部。", example = "Spring Boot")
	val keyword: String?,

	@field:Schema(description = "按文章状态筛选。不传则查询全部。", example = "published")
	val status: ArticleStatus?,

	@field:Schema(description = "按作者 ID 筛选。不传则查询全部。", example = "2")
	val authorId: Long?,
)

data class CreateArticleRequest(
	@field:Schema(description = "作者用户 ID。后续接入登录后可替换为当前登录用户。", example = "2")
	val authorId: Long,

	@field:Schema(description = "文章标题", example = "我的第一篇博客")
	@field:NotBlank
	@field:Size(max = 200)
	val title: String,

	@field:Schema(description = "URL 友好标识，不传则由标题自动生成", example = "my-first-blog")
	@field:Size(max = 220)
	val slug: String?,

	@field:Schema(description = "文章摘要", example = "这是一篇博客文章摘要")
	@field:Size(max = 500)
	val summary: String?,

	@field:Schema(description = "Markdown 正文", example = "# 标题\\n\\n正文内容")
	@field:NotBlank
	val contentMd: String,

	@field:Schema(description = "渲染后的 HTML，可由前端或后端 Markdown 渲染器生成")
	val contentHtml: String?,

	@field:Schema(description = "封面图地址", example = "https://example.com/cover.png")
	@field:Size(max = 500)
	val coverUrl: String?,

	@field:Schema(description = "分类 slug 集合", example = "[\"tech\"]")
	val categorySlugs: Set<String> = emptySet(),

	@field:Schema(description = "标签 slug 集合", example = "[\"kotlin\", \"spring-boot\"]")
	val tagSlugs: Set<String> = emptySet(),
)

data class UpdateArticleRequest(
	@field:Schema(description = "作者用户 ID。后续接入登录后可替换为当前登录用户。", example = "2")
	val authorId: Long,

	@field:Schema(description = "文章标题", example = "更新后的标题")
	@field:NotBlank
	@field:Size(max = 200)
	val title: String,

	@field:Schema(description = "URL 友好标识，不传则由标题自动生成", example = "updated-blog-title")
	@field:Size(max = 220)
	val slug: String?,

	@field:Schema(description = "文章摘要", example = "更新后的摘要")
	@field:Size(max = 500)
	val summary: String?,

	@field:Schema(description = "Markdown 正文", example = "# 更新后的标题\\n\\n正文内容")
	@field:NotBlank
	val contentMd: String,

	@field:Schema(description = "渲染后的 HTML，可由前端或后端 Markdown 渲染器生成")
	val contentHtml: String?,

	@field:Schema(description = "封面图地址", example = "https://example.com/new-cover.png")
	@field:Size(max = 500)
	val coverUrl: String?,

	@field:Schema(description = "分类 slug 集合", example = "[\"tech\"]")
	val categorySlugs: Set<String> = emptySet(),

	@field:Schema(description = "标签 slug 集合", example = "[\"kotlin\"]")
	val tagSlugs: Set<String> = emptySet(),
)

data class AdminUpdateArticleRequest(
	@field:Schema(description = "管理员用户 ID。后续接入登录后可替换为当前登录用户。", example = "2")
	val managerId: Long,

	@field:Schema(description = "文章标题", example = "后台更新后的标题")
	@field:NotBlank
	@field:Size(max = 200)
	val title: String,

	@field:Schema(description = "URL 友好标识，不传则由标题自动生成", example = "admin-updated-blog-title")
	@field:Size(max = 220)
	val slug: String?,

	@field:Schema(description = "文章摘要", example = "后台更新后的摘要")
	@field:Size(max = 500)
	val summary: String?,

	@field:Schema(description = "Markdown 正文", example = "# 后台更新后的标题\\n\\n正文内容")
	@field:NotBlank
	val contentMd: String,

	@field:Schema(description = "渲染后的 HTML，可由前端或后端 Markdown 渲染器生成")
	val contentHtml: String?,

	@field:Schema(description = "封面图地址", example = "https://example.com/new-cover.png")
	@field:Size(max = 500)
	val coverUrl: String?,

	@field:Schema(description = "分类 slug 集合", example = "[\"tech\"]")
	val categorySlugs: Set<String> = emptySet(),

	@field:Schema(description = "标签 slug 集合", example = "[\"kotlin\"]")
	val tagSlugs: Set<String> = emptySet(),
)

data class PublishArticleRequest(
	@field:Schema(description = "作者用户 ID。后续接入登录后可替换为当前登录用户。", example = "2")
	val authorId: Long,
)

data class ManageArticleRequest(
	@field:Schema(description = "管理员用户 ID。后续接入登录后可替换为当前登录用户。", example = "2")
	val managerId: Long,
)

data class DeleteArticleRequest(
	@field:Schema(description = "作者用户 ID。后续接入登录后可替换为当前登录用户。", example = "2")
	val authorId: Long,
)

fun ArticleSummary.toListItem(
	categories: List<Category>,
	tags: List<Tag>,
): ArticleListItemResponse =
	ArticleListItemResponse(
		id = id,
		title = title,
		slug = slug,
		summary = summary,
		coverUrl = coverUrl,
		author = AuthorResponse(authorId, authorName),
		categories = categories.map { it.toResponse() },
		tags = tags.map { it.toResponse() },
		status = status,
		viewCount = viewCount,
		commentCount = commentCount,
		publishedAt = publishedAt,
		createdAt = createdAt,
	)

fun ArticleDetail.toResponse(
	categories: List<Category>,
	tags: List<Tag>,
): ArticleDetailResponse =
	ArticleDetailResponse(
		id = id,
		title = title,
		slug = slug,
		summary = summary,
		contentMd = contentMd,
		contentHtml = contentHtml,
		coverUrl = coverUrl,
		author = AuthorResponse(authorId, authorName),
		categories = categories.map { it.toResponse() },
		tags = tags.map { it.toResponse() },
		status = status,
		viewCount = viewCount,
		commentCount = commentCount,
		publishedAt = publishedAt,
		createdAt = createdAt,
		updatedAt = updatedAt,
	)

fun Category.toResponse(): CategoryResponse =
	CategoryResponse(
		id = id,
		name = name,
		slug = slug,
		description = description,
	)

fun Tag.toResponse(): TagResponse =
	TagResponse(
		id = id,
		name = name,
		slug = slug,
	)

fun AdminUpdateArticleRequest.toAuthoringRequest(): UpdateArticleRequest =
	UpdateArticleRequest(
		authorId = managerId,
		title = title,
		slug = slug,
		summary = summary,
		contentMd = contentMd,
		contentHtml = contentHtml,
		coverUrl = coverUrl,
		categorySlugs = categorySlugs,
		tagSlugs = tagSlugs,
	)

fun Comment.toResponse(): CommentResponse =
	CommentResponse(
		id = id,
		articleId = articleId,
		articleTitle = articleTitle,
		user = AuthorResponse(userId, userName),
		parentId = parentId,
		content = content,
		status = status,
		createdAt = createdAt,
		updatedAt = updatedAt,
	)
