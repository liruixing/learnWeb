package com.dennis.learn.common

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
	@Bean
	fun blogOpenApi(): OpenAPI =
		OpenAPI()
			.info(
				Info()
					.title("Learn Blog API")
					.description("博客网站 MVP 接口文档，当前包含用户模块、内容浏览、内容搜索、文章创作、内容组织、互动功能和后台管理接口。")
					.version("0.0.1")
					.contact(
						Contact()
							.name("Dennis")
					),
			)
}
