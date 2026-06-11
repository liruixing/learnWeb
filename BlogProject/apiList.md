# Blog API 接口清单

## 说明

- 本文档按当前源码中的 Controller 整理。
- 本项目暂未接入真实登录态，涉及权限的接口通过请求参数或请求体中的 `userId`、`authorId`、`managerId` 临时模拟当前登录用户。
- 初始化数据中常用测试账号：
  - `demo_author`：作者 + 管理员，常见 ID 为 `2`
  - `demo_reader`：普通用户，常见 ID 为 `11`
- Swagger 地址：`http://localhost:8080/swagger-ui/index.html`

## 用户模块

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/api/users` | 创建用户，默认分配 `USER` 角色 |
| `GET` | `/api/users` | 查询用户列表，支持状态筛选和分页 |
| `GET` | `/api/users/{id}` | 查询用户详情 |
| `PUT` | `/api/users/{id}` | 更新用户资料 |
| `PATCH` | `/api/users/{id}/status` | 更新用户状态 |
| `PUT` | `/api/users/{id}/roles` | 替换用户角色 |
| `DELETE` | `/api/users/{id}` | 软删除用户 |

常用参数：

- `GET /api/users`：`status` 可选，值为 `active` / `disabled`；`page` 默认 `0`；`size` 默认 `20`。

创建用户请求体：

```json
{
  "username": "new_user",
  "email": "new_user@example.com",
  "phone": "13800001111",
  "password": "Password123",
  "nickname": "新用户",
  "avatarUrl": "https://example.com/avatar.png",
  "bio": "个人简介"
}
```

分配角色请求体：

```json
{
  "roleCodes": ["USER", "AUTHOR"]
}
```

## 内容浏览

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/api/articles` | 首页文章列表，只返回已发布文章 |
| `GET` | `/api/articles/{idOrSlug}` | 文章详情，支持文章 ID 或 slug |
| `GET` | `/api/categories` | 分类列表 |
| `GET` | `/api/categories/{slug}/articles` | 按分类查看文章 |
| `GET` | `/api/tags` | 标签列表 |
| `GET` | `/api/tags/{slug}/articles` | 按标签查看文章 |
| `GET` | `/api/search/articles` | 关键词搜索文章 |

常用参数：

- 文章列表、分类文章、标签文章：`page`、`size`、`sort`，其中 `sort` 为 `latest` / `hot`。
- 搜索文章：`keyword` 必填；`sort` 为 `relevance` / `latest` / `hot`。

示例：

```http
GET /api/search/articles?keyword=Spring&page=0&size=20&sort=relevance
```

## 文章创作

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/api/author/articles` | 作者或管理员创建草稿 |
| `PUT` | `/api/author/articles/{id}` | 作者或管理员编辑文章 |
| `PATCH` | `/api/author/articles/{id}/publish` | 发布文章 |
| `DELETE` | `/api/author/articles/{id}` | 软删除文章 |

创建/编辑文章请求体：

```json
{
  "authorId": 2,
  "title": "我的第一篇博客",
  "slug": "my-first-blog",
  "summary": "文章摘要",
  "contentMd": "# 标题\n\n正文内容",
  "contentHtml": "<h1>标题</h1><p>正文内容</p>",
  "coverUrl": "https://example.com/cover.png",
  "categorySlugs": ["tech"],
  "tagSlugs": ["kotlin", "spring-boot"]
}
```

发布文章请求体：

```json
{
  "authorId": 2
}
```

## 内容组织

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/api/admin/content/categories` | 管理员创建分类 |
| `PUT` | `/api/admin/content/categories/{id}` | 管理员编辑分类 |
| `DELETE` | `/api/admin/content/categories/{id}` | 管理员软删除未被文章使用的分类 |
| `POST` | `/api/admin/content/tags` | 管理员创建标签 |
| `PUT` | `/api/admin/content/tags/{id}` | 管理员编辑标签 |
| `DELETE` | `/api/admin/content/tags/{id}` | 管理员软删除未被文章使用的标签 |

创建分类请求体：

```json
{
  "managerId": 2,
  "name": "读书",
  "slug": "reading",
  "description": "读书笔记、书评和知识整理",
  "sortOrder": 40
}
```

创建标签请求体：

```json
{
  "managerId": 2,
  "name": "架构",
  "slug": "architecture"
}
```

删除分类/标签请求体：

```json
{
  "managerId": 2
}
```

## 互动功能

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/api/articles/{articleId}/comments` | 查询文章可见评论 |
| `POST` | `/api/articles/{articleId}/comments` | 登录用户发表评论或回复评论 |
| `GET` | `/api/admin/content/comments` | 后台评论列表 |
| `PATCH` | `/api/admin/content/comments/{id}/hide` | 隐藏评论 |
| `PATCH` | `/api/admin/content/comments/{id}/show` | 恢复评论 |
| `DELETE` | `/api/admin/content/comments/{id}` | 软删除评论 |

评论列表参数：

- 前台评论列表：`page` 默认 `0`；`size` 默认 `20`。
- 后台评论列表：`managerId` 必填；`articleId` 可选；`status` 可选，值为 `visible` / `hidden`；`page`、`size` 可选。

发表评论请求体：

```json
{
  "userId": 11,
  "parentId": null,
  "content": "这篇文章讲得很清楚。"
}
```

隐藏、恢复、删除评论请求体：

```json
{
  "managerId": 2
}
```

## 后台文章管理

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/api/admin/content/articles` | 管理员查看全部文章 |
| `GET` | `/api/admin/content/articles/{id}` | 管理员查看文章详情 |
| `PUT` | `/api/admin/content/articles/{id}` | 管理员编辑任意文章 |
| `PATCH` | `/api/admin/content/articles/{id}/publish` | 管理员上架文章 |
| `PATCH` | `/api/admin/content/articles/{id}/offline` | 管理员下架文章 |
| `DELETE` | `/api/admin/content/articles/{id}` | 管理员软删除文章 |

后台文章列表参数：

- `managerId` 必填。
- `keyword` 可选，按标题、摘要、正文筛选。
- `status` 可选，值为 `draft` / `published` / `offline`。
- `authorId` 可选。
- `page` 默认 `0`；`size` 默认 `20`。

后台编辑文章请求体：

```json
{
  "managerId": 2,
  "title": "后台更新后的标题",
  "slug": "admin-updated-blog-title",
  "summary": "后台更新后的摘要",
  "contentMd": "# 后台更新后的标题\n\n正文内容",
  "contentHtml": "<h1>后台更新后的标题</h1><p>正文内容</p>",
  "coverUrl": "https://example.com/new-cover.png",
  "categorySlugs": ["tech"],
  "tagSlugs": ["kotlin"]
}
```

上架、下架、删除文章请求体：

```json
{
  "managerId": 2
}
```

## 后台用户管理

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/api/admin/users` | 管理员查看用户列表 |
| `GET` | `/api/admin/users/{id}` | 管理员查看用户详情 |
| `PUT` | `/api/admin/users/{id}` | 管理员更新用户资料 |
| `PATCH` | `/api/admin/users/{id}/status` | 管理员启用或禁用用户 |
| `PUT` | `/api/admin/users/{id}/roles` | 管理员调整用户角色 |
| `DELETE` | `/api/admin/users/{id}` | 管理员软删除用户 |

后台用户列表参数：

- `managerId` 必填。
- `status` 可选，值为 `active` / `disabled`。
- `page` 默认 `0`；`size` 默认 `20`。

后台更新用户资料：

```http
PUT /api/admin/users/{id}?managerId=2
```

```json
{
  "email": "new-user@example.com",
  "phone": "13900000000",
  "nickname": "新昵称",
  "avatarUrl": "https://example.com/new-avatar.png",
  "bio": "新的个人简介"
}
```

后台更新用户状态请求体：

```json
{
  "managerId": 2,
  "status": "disabled"
}
```

后台调整用户角色请求体：

```json
{
  "managerId": 2,
  "roleCodes": ["USER", "AUTHOR"]
}
```

后台删除用户请求体：

```json
{
  "managerId": 2
}
```

## 常见状态码

| 状态码 | 含义 |
| --- | --- |
| `200` | 请求成功 |
| `201` | 创建成功 |
| `204` | 删除成功，无响应体 |
| `400` | 请求参数不合法 |
| `403` | 当前用户无权限 |
| `404` | 资源不存在，或前台访问了未发布/已下架内容 |
| `409` | 唯一键冲突，例如用户名、分类名、slug 重复 |
