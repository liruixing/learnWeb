package com.dennis.learn.content

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/content")
@Tag(name = "内容组织", description = "分类管理和标签管理")
class ContentOrganizationController(
	private val contentService: ContentService,
) {
	@PostMapping("/categories")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "创建分类", description = "管理员创建文章分类，用于内容组织和分类浏览。")
	fun createCategory(@Valid @RequestBody request: CreateCategoryRequest): CategoryResponse =
		contentService.createCategory(request)

	@PutMapping("/categories/{id}")
	@Operation(summary = "编辑分类", description = "管理员编辑分类名称、slug、描述和排序。")
	fun updateCategory(
		@PathVariable id: Long,
		@Valid @RequestBody request: UpdateCategoryRequest,
	): CategoryResponse =
		contentService.updateCategory(id, request)

	@DeleteMapping("/categories/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "删除分类", description = "管理员软删除未被文章使用的分类。")
	fun deleteCategory(
		@PathVariable id: Long,
		@RequestBody request: DeleteCategoryRequest,
	) {
		contentService.deleteCategory(id, request)
	}

	@PostMapping("/tags")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "创建标签", description = "管理员创建文章标签，用于内容组织和标签浏览。")
	fun createTag(@Valid @RequestBody request: CreateTagRequest): TagResponse =
		contentService.createTag(request)

	@PutMapping("/tags/{id}")
	@Operation(summary = "编辑标签", description = "管理员编辑标签名称和 slug。")
	fun updateTag(
		@PathVariable id: Long,
		@Valid @RequestBody request: UpdateTagRequest,
	): TagResponse =
		contentService.updateTag(id, request)

	@DeleteMapping("/tags/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "删除标签", description = "管理员软删除未被文章使用的标签。")
	fun deleteTag(
		@PathVariable id: Long,
		@RequestBody request: DeleteTagRequest,
	) {
		contentService.deleteTag(id, request)
	}
}
