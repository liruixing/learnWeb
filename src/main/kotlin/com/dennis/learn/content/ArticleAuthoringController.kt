package com.dennis.learn.content

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/author/articles")
@Tag(name = "文章创作", description = "Markdown 草稿、发布、编辑和删除文章")
class ArticleAuthoringController(
	private val contentService: ContentService,
) {
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "创建文章草稿", description = "作者或管理员创建一篇草稿文章，支持 Markdown、分类和标签。")
	fun createDraftArticle(@Valid @RequestBody request: CreateArticleRequest): ArticleDetailResponse =
		contentService.createDraftArticle(request)

	@PutMapping("/{id}")
	@Operation(summary = "编辑文章", description = "作者或管理员编辑草稿或已发布文章的标题、正文、分类和标签。")
	fun updateArticle(
		@PathVariable id: Long,
		@Valid @RequestBody request: UpdateArticleRequest,
	): ArticleDetailResponse =
		contentService.updateArticle(id, request)

	@PatchMapping("/{id}/publish")
	@Operation(summary = "发布文章", description = "将草稿或下架文章发布到前台，首次发布时自动写入发布时间。")
	fun publishArticle(
		@PathVariable id: Long,
		@RequestBody request: PublishArticleRequest,
	): ArticleDetailResponse =
		contentService.publishArticle(id, request)

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "删除文章", description = "作者或管理员软删除文章，保留历史数据。")
	fun deleteArticle(
		@PathVariable id: Long,
		@RequestBody request: DeleteArticleRequest,
	) {
		contentService.deleteArticle(id, request)
	}
}
