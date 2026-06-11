package com.dennis.learn.content

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
@Tag(name = "互动功能", description = "文章评论和评论管理")
class CommentController(
	private val contentService: ContentService,
) {
	@GetMapping("/articles/{articleId}/comments")
	@Operation(summary = "文章评论列表", description = "查询已发布文章下的可见评论，支持分页。")
	fun listArticleComments(
		@PathVariable articleId: Long,
		@RequestParam(defaultValue = "0") page: Int,
		@RequestParam(defaultValue = "20") size: Int,
	): CommentPageResponse =
		contentService.listArticleComments(articleId, page, size)

	@PostMapping("/articles/{articleId}/comments")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "评论文章", description = "登录用户对已发布文章发表评论，可选择回复某条评论。")
	fun createComment(
		@PathVariable articleId: Long,
		@Valid @RequestBody request: CreateCommentRequest,
	): CommentResponse =
		contentService.createComment(articleId, request)

	@GetMapping("/admin/content/comments")
	@Operation(summary = "后台评论列表", description = "管理员查看全部评论；作者查看自己文章下的评论。")
	fun listManagedComments(
		@RequestParam managerId: Long,
		@RequestParam(required = false) articleId: Long?,
		@RequestParam(required = false) status: CommentStatus?,
		@RequestParam(defaultValue = "0") page: Int,
		@RequestParam(defaultValue = "20") size: Int,
	): CommentPageResponse =
		contentService.listManagedComments(
			managerId = managerId,
			query = CommentListQuery(page, size, articleId, status),
		)

	@PatchMapping("/admin/content/comments/{id}/hide")
	@Operation(summary = "隐藏评论", description = "文章作者或管理员隐藏不合规评论，前台不再展示。")
	fun hideComment(
		@PathVariable id: Long,
		@RequestBody request: ManageCommentRequest,
	): CommentResponse =
		contentService.hideComment(id, request)

	@PatchMapping("/admin/content/comments/{id}/show")
	@Operation(summary = "恢复评论", description = "文章作者或管理员恢复已隐藏评论。")
	fun showComment(
		@PathVariable id: Long,
		@RequestBody request: ManageCommentRequest,
	): CommentResponse =
		contentService.showComment(id, request)

	@DeleteMapping("/admin/content/comments/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "删除评论", description = "文章作者或管理员软删除评论。")
	fun deleteComment(
		@PathVariable id: Long,
		@RequestBody request: ManageCommentRequest,
	) {
		contentService.deleteComment(id, request)
	}
}
