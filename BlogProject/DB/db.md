# 博客网站 MVP 数据库设计

## 1. 设计目标

本数据库设计基于博客网站 MVP 范围，覆盖以下能力：

| 模块 | 覆盖能力 |
| --- | --- |
| 内容浏览 | 首页文章列表、文章详情、分类浏览、标签浏览、上一篇/下一篇 |
| 内容搜索 | 按标题、摘要、正文关键词搜索文章 |
| 文章创作 | Markdown 编辑、草稿、发布、编辑、删除 |
| 内容组织 | 分类管理、标签管理 |
| 用户系统 | 注册、登录、退出、角色权限 |
| 互动功能 | 评论、评论管理 |
| 后台管理 | 文章、分类、标签、用户、评论管理 |

## 2. 数据库选型建议

建议 MVP 阶段使用关系型数据库：

| 数据库 | 说明 |
| --- | --- |
| MySQL 8.x | 使用广泛，适合 Spring Boot 项目快速落地 |
| PostgreSQL 15+ | 全文搜索和 JSON 能力更强，后续扩展空间更好 |

如果项目没有强依赖，建议优先使用 MySQL 8.x，降低学习和部署成本。

## 3. 核心实体关系

```text
user 1 --- n article
user 1 --- n comment

role n --- n user

article n --- n category
article n --- n tag
article 1 --- n comment

comment 1 --- n comment
```

说明：

| 关系 | 说明 |
| --- | --- |
| 用户与文章 | 一个作者可以发布多篇文章 |
| 用户与评论 | 一个用户可以发布多条评论 |
| 用户与角色 | 一个用户可以拥有多个角色，例如 USER、AUTHOR、ADMIN |
| 文章与分类 | MVP 可支持一篇文章绑定多个分类，也可在业务层限制为一个分类 |
| 文章与标签 | 一篇文章可绑定多个标签 |
| 文章与评论 | 一篇文章可拥有多条评论 |
| 评论与评论 | 支持评论回复，使用 parent_id 表示父评论 |

## 4. 表清单

| 表名 | 中文名 | 说明 |
| --- | --- | --- |
| users | 用户表 | 存储注册用户、作者、管理员基础信息 |
| roles | 角色表 | 存储系统角色 |
| user_roles | 用户角色关联表 | 用户和角色多对多关系 |
| articles | 文章表 | 存储文章主体信息 |
| categories | 分类表 | 存储文章分类 |
| article_categories | 文章分类关联表 | 文章和分类多对多关系 |
| tags | 标签表 | 存储文章标签 |
| article_tags | 文章标签关联表 | 文章和标签多对多关系 |
| comments | 评论表 | 存储文章评论和回复 |

## 5. 通用字段约定

| 字段 | 说明 |
| --- | --- |
| id | 主键，建议使用 BIGINT 自增或雪花 ID |
| created_at | 创建时间 |
| updated_at | 更新时间 |
| deleted_at | 删除时间，非空表示软删除 |
| created_by | 创建人用户 ID，适用于后台管理数据 |
| updated_by | 最后更新人用户 ID，适用于后台管理数据 |

状态字段建议使用字符串或小整数枚举。MVP 阶段推荐字符串，便于阅读和调试。

## 6. 表结构设计

### 6.1 users 用户表

用于注册、登录、用户管理、作者信息展示。

| 字段 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | BIGINT | 是 | 自增 | 用户 ID |
| username | VARCHAR(50) | 是 |  | 登录用户名，全局唯一 |
| email | VARCHAR(100) | 否 |  | 邮箱，全局唯一 |
| phone | VARCHAR(30) | 否 |  | 手机号，全局唯一 |
| password_hash | VARCHAR(255) | 是 |  | 加密后的密码 |
| nickname | VARCHAR(50) | 是 |  | 前台展示昵称 |
| avatar_url | VARCHAR(500) | 否 |  | 用户头像地址 |
| bio | VARCHAR(500) | 否 |  | 用户简介 |
| status | VARCHAR(20) | 是 | active | 用户状态：active、disabled |
| last_login_at | DATETIME | 否 |  | 最近登录时间 |
| created_at | DATETIME | 是 | 当前时间 | 创建时间 |
| updated_at | DATETIME | 是 | 当前时间 | 更新时间 |
| deleted_at | DATETIME | 否 |  | 软删除时间 |

索引建议：

| 索引 | 字段 | 说明 |
| --- | --- | --- |
| uk_users_username | username | 用户名唯一 |
| uk_users_email | email | 邮箱唯一 |
| uk_users_phone | phone | 手机号唯一 |
| idx_users_status | status | 后台按状态筛选 |

### 6.2 roles 角色表

用于角色权限控制。

| 字段 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | BIGINT | 是 | 自增 | 角色 ID |
| code | VARCHAR(50) | 是 |  | 角色编码，如 USER、AUTHOR、ADMIN |
| name | VARCHAR(50) | 是 |  | 角色名称 |
| description | VARCHAR(255) | 否 |  | 角色说明 |
| created_at | DATETIME | 是 | 当前时间 | 创建时间 |
| updated_at | DATETIME | 是 | 当前时间 | 更新时间 |

初始化角色：

| code | name | 说明 |
| --- | --- | --- |
| USER | 普通用户 | 可浏览、评论 |
| AUTHOR | 作者 | 可创建和管理自己的文章 |
| ADMIN | 管理员 | 可管理全站内容和用户 |

索引建议：

| 索引 | 字段 | 说明 |
| --- | --- | --- |
| uk_roles_code | code | 角色编码唯一 |

### 6.3 user_roles 用户角色关联表

用于用户和角色的多对多关系。

| 字段 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | BIGINT | 是 | 自增 | 关联 ID |
| user_id | BIGINT | 是 |  | 用户 ID |
| role_id | BIGINT | 是 |  | 角色 ID |
| created_at | DATETIME | 是 | 当前时间 | 创建时间 |

索引建议：

| 索引 | 字段 | 说明 |
| --- | --- | --- |
| uk_user_roles_user_role | user_id、role_id | 防止重复授权 |
| idx_user_roles_role_id | role_id | 按角色查询用户 |

### 6.4 articles 文章表

用于文章列表、详情、搜索、草稿、发布、删除和后台文章管理。

| 字段 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | BIGINT | 是 | 自增 | 文章 ID |
| author_id | BIGINT | 是 |  | 作者用户 ID |
| title | VARCHAR(200) | 是 |  | 文章标题 |
| slug | VARCHAR(220) | 否 |  | URL 友好标识，可用于 SEO |
| summary | VARCHAR(500) | 否 |  | 文章摘要 |
| content_md | LONGTEXT | 是 |  | Markdown 原文 |
| content_html | LONGTEXT | 否 |  | 渲染后的 HTML，可按需缓存 |
| cover_url | VARCHAR(500) | 否 |  | 封面图地址 |
| status | VARCHAR(20) | 是 | draft | 文章状态：draft、published、offline |
| view_count | BIGINT | 是 | 0 | 阅读量 |
| comment_count | BIGINT | 是 | 0 | 评论数冗余字段 |
| published_at | DATETIME | 否 |  | 发布时间 |
| created_at | DATETIME | 是 | 当前时间 | 创建时间 |
| updated_at | DATETIME | 是 | 当前时间 | 更新时间 |
| deleted_at | DATETIME | 否 |  | 软删除时间 |

状态说明：

| 状态 | 说明 |
| --- | --- |
| draft | 草稿，仅作者和管理员可见 |
| published | 已发布，前台可见 |
| offline | 已下架，前台不可见 |

索引建议：

| 索引 | 字段 | 说明 |
| --- | --- | --- |
| uk_articles_slug | slug | 文章 URL 标识唯一 |
| idx_articles_author_id | author_id | 查询作者文章 |
| idx_articles_status_published_at | status、published_at | 首页列表、后台筛选 |
| idx_articles_created_at | created_at | 后台按创建时间排序 |
| ft_articles_search | title、summary、content_md | 文章关键词搜索，MySQL 可建 FULLTEXT |

### 6.5 categories 分类表

用于分类浏览和后台分类管理。

| 字段 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | BIGINT | 是 | 自增 | 分类 ID |
| name | VARCHAR(50) | 是 |  | 分类名称 |
| slug | VARCHAR(80) | 是 |  | 分类 URL 标识 |
| description | VARCHAR(255) | 否 |  | 分类描述 |
| sort_order | INT | 是 | 0 | 排序值，越小越靠前 |
| status | VARCHAR(20) | 是 | active | 状态：active、disabled |
| created_at | DATETIME | 是 | 当前时间 | 创建时间 |
| updated_at | DATETIME | 是 | 当前时间 | 更新时间 |
| deleted_at | DATETIME | 否 |  | 软删除时间 |

索引建议：

| 索引 | 字段 | 说明 |
| --- | --- | --- |
| uk_categories_name | name | 分类名称唯一 |
| uk_categories_slug | slug | 分类标识唯一 |
| idx_categories_status_sort | status、sort_order | 前台分类展示 |

### 6.6 article_categories 文章分类关联表

用于文章与分类的多对多关系。

| 字段 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | BIGINT | 是 | 自增 | 关联 ID |
| article_id | BIGINT | 是 |  | 文章 ID |
| category_id | BIGINT | 是 |  | 分类 ID |
| created_at | DATETIME | 是 | 当前时间 | 创建时间 |

索引建议：

| 索引 | 字段 | 说明 |
| --- | --- | --- |
| uk_article_categories_article_category | article_id、category_id | 防止重复绑定 |
| idx_article_categories_category_id | category_id | 分类文章列表 |

### 6.7 tags 标签表

用于标签浏览和后台标签管理。

| 字段 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | BIGINT | 是 | 自增 | 标签 ID |
| name | VARCHAR(50) | 是 |  | 标签名称 |
| slug | VARCHAR(80) | 是 |  | 标签 URL 标识 |
| status | VARCHAR(20) | 是 | active | 状态：active、disabled |
| created_at | DATETIME | 是 | 当前时间 | 创建时间 |
| updated_at | DATETIME | 是 | 当前时间 | 更新时间 |
| deleted_at | DATETIME | 否 |  | 软删除时间 |

索引建议：

| 索引 | 字段 | 说明 |
| --- | --- | --- |
| uk_tags_name | name | 标签名称唯一 |
| uk_tags_slug | slug | 标签标识唯一 |
| idx_tags_status | status | 后台筛选 |

### 6.8 article_tags 文章标签关联表

用于文章与标签的多对多关系。

| 字段 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | BIGINT | 是 | 自增 | 关联 ID |
| article_id | BIGINT | 是 |  | 文章 ID |
| tag_id | BIGINT | 是 |  | 标签 ID |
| created_at | DATETIME | 是 | 当前时间 | 创建时间 |

索引建议：

| 索引 | 字段 | 说明 |
| --- | --- | --- |
| uk_article_tags_article_tag | article_id、tag_id | 防止重复绑定 |
| idx_article_tags_tag_id | tag_id | 标签文章列表 |

### 6.9 comments 评论表

用于文章评论、评论回复、评论管理。

| 字段 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | BIGINT | 是 | 自增 | 评论 ID |
| article_id | BIGINT | 是 |  | 所属文章 ID |
| user_id | BIGINT | 是 |  | 评论用户 ID |
| parent_id | BIGINT | 否 |  | 父评论 ID，空表示一级评论 |
| root_id | BIGINT | 否 |  | 根评论 ID，便于查询评论楼层 |
| content | TEXT | 是 |  | 评论内容 |
| status | VARCHAR(20) | 是 | published | 评论状态：published、hidden、deleted |
| created_at | DATETIME | 是 | 当前时间 | 创建时间 |
| updated_at | DATETIME | 是 | 当前时间 | 更新时间 |
| deleted_at | DATETIME | 否 |  | 软删除时间 |

状态说明：

| 状态 | 说明 |
| --- | --- |
| published | 正常展示 |
| hidden | 管理员隐藏，前台不展示 |
| deleted | 用户或管理员删除 |

索引建议：

| 索引 | 字段 | 说明 |
| --- | --- | --- |
| idx_comments_article_status_created | article_id、status、created_at | 查询文章评论列表 |
| idx_comments_user_id | user_id | 查询用户评论 |
| idx_comments_parent_id | parent_id | 查询评论回复 |
| idx_comments_root_id | root_id | 查询同一评论线程 |

## 7. 关键查询场景支持

### 7.1 首页文章列表

依赖表：

| 表 | 用途 |
| --- | --- |
| articles | 查询已发布文章 |
| users | 展示作者昵称和头像 |
| article_categories、categories | 展示分类 |
| article_tags、tags | 展示标签 |

核心筛选条件：

```sql
articles.status = 'published'
articles.deleted_at IS NULL
ORDER BY articles.published_at DESC
```

### 7.2 文章详情

依赖表：

| 表 | 用途 |
| --- | --- |
| articles | 展示文章正文 |
| users | 展示作者信息 |
| categories | 展示分类 |
| tags | 展示标签 |
| comments | 展示评论列表 |

### 7.3 分类文章列表

依赖表：

| 表 | 用途 |
| --- | --- |
| categories | 定位分类 |
| article_categories | 查询分类下文章 ID |
| articles | 查询已发布文章 |

### 7.4 标签文章列表

依赖表：

| 表 | 用途 |
| --- | --- |
| tags | 定位标签 |
| article_tags | 查询标签下文章 ID |
| articles | 查询已发布文章 |

### 7.5 关键词搜索

MVP 可先使用数据库全文索引或 LIKE 查询：

| 方案 | 说明 |
| --- | --- |
| LIKE | 实现简单，适合数据量小的 MVP 阶段 |
| FULLTEXT | MySQL 全文索引，适合中等数据量 |
| Elasticsearch | 非 MVP 必需，后续内容量变大后再引入 |

搜索字段：

| 字段 | 权重建议 |
| --- | --- |
| title | 高 |
| summary | 中 |
| content_md | 低 |

### 7.6 上一篇/下一篇

按发布时间查找相邻文章：

| 功能 | 条件 |
| --- | --- |
| 上一篇 | published_at 小于当前文章发布时间，按 published_at DESC 取 1 条 |
| 下一篇 | published_at 大于当前文章发布时间，按 published_at ASC 取 1 条 |

只查询：

```sql
status = 'published'
deleted_at IS NULL
```

## 8. 外键建议

MVP 阶段建议在数据库层添加外键，保证数据一致性。

| 表 | 外键 |
| --- | --- |
| user_roles | user_id -> users.id |
| user_roles | role_id -> roles.id |
| articles | author_id -> users.id |
| article_categories | article_id -> articles.id |
| article_categories | category_id -> categories.id |
| article_tags | article_id -> articles.id |
| article_tags | tag_id -> tags.id |
| comments | article_id -> articles.id |
| comments | user_id -> users.id |
| comments | parent_id -> comments.id |
| comments | root_id -> comments.id |

删除策略建议：

| 场景 | 策略 |
| --- | --- |
| 删除用户 | 不物理删除，使用 disabled 或 deleted_at |
| 删除文章 | 软删除文章，同时前台不展示评论 |
| 删除分类 | 若有关联文章，禁止删除或先解绑 |
| 删除标签 | 可先解绑再软删除 |
| 删除评论 | 软删除或状态改为 deleted |

## 9. 数据一致性规则

| 规则 | 说明 |
| --- | --- |
| 用户名唯一 | users.username 不允许重复 |
| 文章 slug 唯一 | 若启用 slug，articles.slug 不允许重复 |
| 分类名称唯一 | categories.name 不允许重复 |
| 标签名称唯一 | tags.name 不允许重复 |
| 文章必须有作者 | articles.author_id 必须存在 |
| 已发布文章必须有发布时间 | status 为 published 时，published_at 必须有值 |
| 评论必须绑定文章和用户 | comments.article_id、comments.user_id 必填 |
| 删除使用软删除 | 核心业务数据不建议直接物理删除 |

## 10. 后续扩展预留

以下能力不属于 MVP，但当前设计已预留扩展空间：

| 后续能力 | 扩展方式 |
| --- | --- |
| 点赞文章 | 新增 article_likes 表 |
| 收藏文章 | 新增 article_favorites 表 |
| 文章审核 | articles 增加 review_status 或新增 article_reviews 表 |
| 定时发布 | articles 增加 scheduled_publish_at |
| 文章版本 | 新增 article_versions 表 |
| 站内通知 | 新增 notifications 表 |
| 数据统计 | 新增 article_stats 或 visit_logs 表 |
| 附件管理 | 新增 files 表 |
| 站点配置 | 新增 site_settings 表 |

## 11. 建议初始化数据

### 11.1 角色数据

| code | name | description |
| --- | --- | --- |
| USER | 普通用户 | 可浏览文章、发表评论 |
| AUTHOR | 作者 | 可创建、编辑、发布自己的文章 |
| ADMIN | 管理员 | 可管理全站用户、文章、分类、标签和评论 |

### 11.2 默认分类数据

| name | slug | description |
| --- | --- | --- |
| 技术 | tech | 技术学习、开发实践、架构设计 |
| 产品 | product | 产品思考、需求分析、项目复盘 |
| 生活 | life | 日常记录、个人成长、随笔 |

## 12. MVP 表优先级

| 优先级 | 表 |
| --- | --- |
| 第一批 | users、roles、user_roles |
| 第二批 | articles、categories、article_categories、tags、article_tags |
| 第三批 | comments |

建议先完成用户和角色，再完成文章发布闭环，最后接入评论和后台管理。
