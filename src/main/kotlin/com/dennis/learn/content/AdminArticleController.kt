package com.dennis.learn.content

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
@RequestMapping("/api/admin/content/articles")
@Tag(name = "后台文章管理", description = "查看、搜索、编辑、删除、上下架全部文章")
class AdminArticleController(
	private val contentService: ContentService,
) {
	@GetMapping
	@Operation(summary = "后台文章列表", description = "管理员查看全部文章，支持关键词、状态和作者筛选。")
	fun listArticles(
		@RequestParam managerId: Long,
		@RequestParam(required = false) keyword: String?,
		@RequestParam(required = false) status: ArticleStatus?,
		@RequestParam(required = false) authorId: Long?,
		@RequestParam(defaultValue = "0") page: Int,
		@RequestParam(defaultValue = "20") size: Int,
	): ArticlePageResponse =
		contentService.listManagedArticles(
			managerId = managerId,
			query = AdminArticleListQuery(page, size, keyword, status, authorId),
		)

	@GetMapping("/{id}")
	@Operation(summary = "后台文章详情", description = "管理员查看任意未删除文章详情，包括草稿、已发布和下架文章。")
	fun getArticle(
		@PathVariable id: Long,
		@RequestParam managerId: Long,
	): ArticleDetailResponse =
		contentService.getManagedArticle(managerId, id)

	@PutMapping("/{id}")
	@Operation(summary = "后台编辑文章", description = "管理员编辑任意文章的标题、正文、分类和标签。")
	fun updateArticle(
		@PathVariable id: Long,
		@Valid @RequestBody request: AdminUpdateArticleRequest,
	): ArticleDetailResponse =
		contentService.updateManagedArticle(id, request)

	@PatchMapping("/{id}/publish")
	@Operation(summary = "后台上架文章", description = "管理员将草稿或下架文章发布到前台。")
	fun publishArticle(
		@PathVariable id: Long,
		@RequestBody request: ManageArticleRequest,
	): ArticleDetailResponse =
		contentService.publishManagedArticle(id, request)

	@PatchMapping("/{id}/offline")
	@Operation(summary = "后台下架文章", description = "管理员将已发布文章下架，前台不再展示。")
	fun offlineArticle(
		@PathVariable id: Long,
		@RequestBody request: ManageArticleRequest,
	): ArticleDetailResponse =
		contentService.offlineManagedArticle(id, request)

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "后台删除文章", description = "管理员软删除任意文章。")
	fun deleteArticle(
		@PathVariable id: Long,
		@RequestBody request: ManageArticleRequest,
	) {
		contentService.deleteManagedArticle(id, request)
	}
}
