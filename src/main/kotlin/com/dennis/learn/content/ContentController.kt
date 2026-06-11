package com.dennis.learn.content

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
@Tag(name = "内容浏览", description = "文章列表、文章详情、分类浏览、标签浏览和内容搜索")
class ContentController(
	private val contentService: ContentService,
) {
	@GetMapping("/articles")
	@Operation(summary = "首页文章列表", description = "展示已发布文章，支持分页和最新/热门排序。")
	fun listArticles(
		@RequestParam(defaultValue = "0") page: Int,
		@RequestParam(defaultValue = "20") size: Int,
		@RequestParam(defaultValue = "latest") sort: ArticleSort,
	): ArticlePageResponse =
		contentService.listArticles(ArticleListQuery(page, size, sort))

	@GetMapping("/articles/{idOrSlug}")
	@Operation(summary = "文章详情页", description = "根据文章 ID 或 slug 查询已发布文章详情。")
	fun getArticle(@PathVariable idOrSlug: String): ArticleDetailResponse =
		contentService.getArticle(idOrSlug)

	@GetMapping("/categories")
	@Operation(summary = "分类列表", description = "查询可用于内容浏览的全部启用分类。")
	fun listCategories(): List<CategoryResponse> =
		contentService.listCategories()

	@GetMapping("/categories/{slug}/articles")
	@Operation(summary = "分类浏览", description = "按分类 slug 查询已发布文章。")
	fun listArticlesByCategory(
		@PathVariable slug: String,
		@RequestParam(defaultValue = "0") page: Int,
		@RequestParam(defaultValue = "20") size: Int,
		@RequestParam(defaultValue = "latest") sort: ArticleSort,
	): ArticlePageResponse =
		contentService.listArticlesByCategory(slug, ArticleListQuery(page, size, sort))

	@GetMapping("/tags")
	@Operation(summary = "标签列表", description = "查询可用于内容浏览的全部启用标签。")
	fun listTags(): List<TagResponse> =
		contentService.listTags()

	@GetMapping("/tags/{slug}/articles")
	@Operation(summary = "标签浏览", description = "按标签 slug 查询已发布文章。")
	fun listArticlesByTag(
		@PathVariable slug: String,
		@RequestParam(defaultValue = "0") page: Int,
		@RequestParam(defaultValue = "20") size: Int,
		@RequestParam(defaultValue = "latest") sort: ArticleSort,
	): ArticlePageResponse =
		contentService.listArticlesByTag(slug, ArticleListQuery(page, size, sort))

	@GetMapping("/search/articles")
	@Operation(summary = "关键词搜索", description = "按标题、摘要和正文搜索已发布文章，返回搜索结果页数据。")
	fun searchArticles(
		@RequestParam keyword: String,
		@RequestParam(defaultValue = "0") page: Int,
		@RequestParam(defaultValue = "20") size: Int,
		@RequestParam(defaultValue = "relevance") sort: ArticleSearchSort,
	): ArticleSearchResponse =
		contentService.searchArticles(ArticleSearchQuery(keyword, page, size, sort))
}
