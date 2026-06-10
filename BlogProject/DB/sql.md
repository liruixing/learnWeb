# 博客网站 MVP 建库建表 SQL

说明：以下 SQL 基于 MySQL 8.x 设计，字符集使用 `utf8mb4`，存储引擎使用 `InnoDB`。

## 1. 创建数据库

```sql
-- 创建博客数据库，使用 utf8mb4 支持中文和 Emoji。
CREATE DATABASE IF NOT EXISTS blog_mvp
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

-- 切换到博客数据库。
USE blog_mvp;
```

## 2. 用户与权限表

```sql
-- 创建用户表，用于注册、登录、作者展示和后台用户管理。
CREATE TABLE IF NOT EXISTS users (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户 ID',
  username VARCHAR(50) NOT NULL COMMENT '登录用户名，全局唯一',
  email VARCHAR(100) NULL COMMENT '邮箱，全局唯一',
  phone VARCHAR(30) NULL COMMENT '手机号，全局唯一',
  password_hash VARCHAR(255) NOT NULL COMMENT '加密后的密码',
  nickname VARCHAR(50) NOT NULL COMMENT '前台展示昵称',
  avatar_url VARCHAR(500) NULL COMMENT '用户头像地址',
  bio VARCHAR(500) NULL COMMENT '用户简介',
  status VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '用户状态：active、disabled',
  last_login_at DATETIME NULL COMMENT '最近登录时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_at DATETIME NULL COMMENT '软删除时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_username (username),
  UNIQUE KEY uk_users_email (email),
  UNIQUE KEY uk_users_phone (phone),
  KEY idx_users_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

-- 创建角色表，用于区分普通用户、作者和管理员。
CREATE TABLE IF NOT EXISTS roles (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色 ID',
  code VARCHAR(50) NOT NULL COMMENT '角色编码，如 USER、AUTHOR、ADMIN',
  name VARCHAR(50) NOT NULL COMMENT '角色名称',
  description VARCHAR(255) NULL COMMENT '角色说明',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_roles_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色表';

-- 创建用户角色关联表，用于维护用户和角色的多对多关系。
CREATE TABLE IF NOT EXISTS user_roles (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联 ID',
  user_id BIGINT NOT NULL COMMENT '用户 ID',
  role_id BIGINT NOT NULL COMMENT '角色 ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_roles_user_role (user_id, role_id),
  KEY idx_user_roles_role_id (role_id),
  CONSTRAINT fk_user_roles_user_id FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_user_roles_role_id FOREIGN KEY (role_id) REFERENCES roles (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户角色关联表';
```

## 3. 文章表

```sql
-- 创建文章表，用于文章草稿、发布、编辑、删除、列表、详情和搜索。
CREATE TABLE IF NOT EXISTS articles (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '文章 ID',
  author_id BIGINT NOT NULL COMMENT '作者用户 ID',
  title VARCHAR(200) NOT NULL COMMENT '文章标题',
  slug VARCHAR(220) NULL COMMENT 'URL 友好标识，可用于 SEO',
  summary VARCHAR(500) NULL COMMENT '文章摘要',
  content_md LONGTEXT NOT NULL COMMENT 'Markdown 原文',
  content_html LONGTEXT NULL COMMENT '渲染后的 HTML，可按需缓存',
  cover_url VARCHAR(500) NULL COMMENT '封面图地址',
  status VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT '文章状态：draft、published、offline',
  view_count BIGINT NOT NULL DEFAULT 0 COMMENT '阅读量',
  comment_count BIGINT NOT NULL DEFAULT 0 COMMENT '评论数冗余字段',
  published_at DATETIME NULL COMMENT '发布时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_at DATETIME NULL COMMENT '软删除时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_articles_slug (slug),
  KEY idx_articles_author_id (author_id),
  KEY idx_articles_status_published_at (status, published_at),
  KEY idx_articles_created_at (created_at),
  FULLTEXT KEY ft_articles_search (title, summary, content_md),
  CONSTRAINT fk_articles_author_id FOREIGN KEY (author_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章表';
```

## 4. 分类表与文章分类关联表

```sql
-- 创建分类表，用于文章分类浏览和后台分类管理。
CREATE TABLE IF NOT EXISTS categories (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类 ID',
  name VARCHAR(50) NOT NULL COMMENT '分类名称',
  slug VARCHAR(80) NOT NULL COMMENT '分类 URL 标识',
  description VARCHAR(255) NULL COMMENT '分类描述',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
  status VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态：active、disabled',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_at DATETIME NULL COMMENT '软删除时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_categories_name (name),
  UNIQUE KEY uk_categories_slug (slug),
  KEY idx_categories_status_sort (status, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='分类表';

-- 创建文章分类关联表，用于维护文章和分类的多对多关系。
CREATE TABLE IF NOT EXISTS article_categories (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联 ID',
  article_id BIGINT NOT NULL COMMENT '文章 ID',
  category_id BIGINT NOT NULL COMMENT '分类 ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_article_categories_article_category (article_id, category_id),
  KEY idx_article_categories_category_id (category_id),
  CONSTRAINT fk_article_categories_article_id FOREIGN KEY (article_id) REFERENCES articles (id),
  CONSTRAINT fk_article_categories_category_id FOREIGN KEY (category_id) REFERENCES categories (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章分类关联表';
```

## 5. 标签表与文章标签关联表

```sql
-- 创建标签表，用于标签浏览和后台标签管理。
CREATE TABLE IF NOT EXISTS tags (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '标签 ID',
  name VARCHAR(50) NOT NULL COMMENT '标签名称',
  slug VARCHAR(80) NOT NULL COMMENT '标签 URL 标识',
  status VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态：active、disabled',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_at DATETIME NULL COMMENT '软删除时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_tags_name (name),
  UNIQUE KEY uk_tags_slug (slug),
  KEY idx_tags_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='标签表';

-- 创建文章标签关联表，用于维护文章和标签的多对多关系。
CREATE TABLE IF NOT EXISTS article_tags (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联 ID',
  article_id BIGINT NOT NULL COMMENT '文章 ID',
  tag_id BIGINT NOT NULL COMMENT '标签 ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_article_tags_article_tag (article_id, tag_id),
  KEY idx_article_tags_tag_id (tag_id),
  CONSTRAINT fk_article_tags_article_id FOREIGN KEY (article_id) REFERENCES articles (id),
  CONSTRAINT fk_article_tags_tag_id FOREIGN KEY (tag_id) REFERENCES tags (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章标签关联表';
```

## 6. 评论表

```sql
-- 创建评论表，用于文章评论、评论回复和后台评论管理。
CREATE TABLE IF NOT EXISTS comments (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论 ID',
  article_id BIGINT NOT NULL COMMENT '所属文章 ID',
  user_id BIGINT NOT NULL COMMENT '评论用户 ID',
  parent_id BIGINT NULL COMMENT '父评论 ID，空表示一级评论',
  root_id BIGINT NULL COMMENT '根评论 ID，便于查询评论线程',
  content TEXT NOT NULL COMMENT '评论内容',
  status VARCHAR(20) NOT NULL DEFAULT 'published' COMMENT '评论状态：published、hidden、deleted',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_at DATETIME NULL COMMENT '软删除时间',
  PRIMARY KEY (id),
  KEY idx_comments_article_status_created (article_id, status, created_at),
  KEY idx_comments_user_id (user_id),
  KEY idx_comments_parent_id (parent_id),
  KEY idx_comments_root_id (root_id),
  CONSTRAINT fk_comments_article_id FOREIGN KEY (article_id) REFERENCES articles (id),
  CONSTRAINT fk_comments_user_id FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_comments_parent_id FOREIGN KEY (parent_id) REFERENCES comments (id),
  CONSTRAINT fk_comments_root_id FOREIGN KEY (root_id) REFERENCES comments (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论表';
```

## 7. 初始化角色数据

```sql
-- 初始化普通用户角色，允许浏览文章和发表评论。
INSERT INTO roles (code, name, description)
VALUES ('USER', '普通用户', '可浏览文章、发表评论')
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  description = VALUES(description),
  updated_at = CURRENT_TIMESTAMP;

-- 初始化作者角色，允许创建、编辑和发布自己的文章。
INSERT INTO roles (code, name, description)
VALUES ('AUTHOR', '作者', '可创建、编辑、发布自己的文章')
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  description = VALUES(description),
  updated_at = CURRENT_TIMESTAMP;

-- 初始化管理员角色，允许管理全站用户、文章、分类、标签和评论。
INSERT INTO roles (code, name, description)
VALUES ('ADMIN', '管理员', '可管理全站用户、文章、分类、标签和评论')
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  description = VALUES(description),
  updated_at = CURRENT_TIMESTAMP;
```

## 8. 初始化默认分类数据

```sql
-- 初始化技术分类，用于技术学习、开发实践、架构设计文章。
INSERT INTO categories (name, slug, description, sort_order, status)
VALUES ('技术', 'tech', '技术学习、开发实践、架构设计', 10, 'active')
ON DUPLICATE KEY UPDATE
  description = VALUES(description),
  sort_order = VALUES(sort_order),
  status = VALUES(status),
  updated_at = CURRENT_TIMESTAMP;

-- 初始化产品分类，用于产品思考、需求分析、项目复盘文章。
INSERT INTO categories (name, slug, description, sort_order, status)
VALUES ('产品', 'product', '产品思考、需求分析、项目复盘', 20, 'active')
ON DUPLICATE KEY UPDATE
  description = VALUES(description),
  sort_order = VALUES(sort_order),
  status = VALUES(status),
  updated_at = CURRENT_TIMESTAMP;

-- 初始化生活分类，用于日常记录、个人成长、随笔文章。
INSERT INTO categories (name, slug, description, sort_order, status)
VALUES ('生活', 'life', '日常记录、个人成长、随笔', 30, 'active')
ON DUPLICATE KEY UPDATE
  description = VALUES(description),
  sort_order = VALUES(sort_order),
  status = VALUES(status),
  updated_at = CURRENT_TIMESTAMP;
```

## 9. 可选检查语句

```sql
-- 查看当前数据库内的全部业务表。
SHOW TABLES;

-- 检查角色初始化结果。
SELECT id, code, name, description, created_at, updated_at
FROM roles
ORDER BY id;

-- 检查默认分类初始化结果。
SELECT id, name, slug, description, sort_order, status
FROM categories
ORDER BY sort_order ASC, id ASC;
```

## 10. 常用查询示例

```sql
-- 查询首页已发布文章列表，按发布时间倒序展示。
SELECT id, author_id, title, slug, summary, cover_url, view_count, comment_count, published_at
FROM articles
WHERE status = 'published'
  AND deleted_at IS NULL
ORDER BY published_at DESC
LIMIT 20 OFFSET 0;

-- 按分类查询已发布文章列表。
SELECT a.id, a.title, a.slug, a.summary, a.published_at
FROM articles a
JOIN article_categories ac ON ac.article_id = a.id
JOIN categories c ON c.id = ac.category_id
WHERE c.slug = 'tech'
  AND c.status = 'active'
  AND c.deleted_at IS NULL
  AND a.status = 'published'
  AND a.deleted_at IS NULL
ORDER BY a.published_at DESC
LIMIT 20 OFFSET 0;

-- 按标签查询已发布文章列表。
SELECT a.id, a.title, a.slug, a.summary, a.published_at
FROM articles a
JOIN article_tags atg ON atg.article_id = a.id
JOIN tags t ON t.id = atg.tag_id
WHERE t.slug = 'spring-boot'
  AND t.status = 'active'
  AND t.deleted_at IS NULL
  AND a.status = 'published'
  AND a.deleted_at IS NULL
ORDER BY a.published_at DESC
LIMIT 20 OFFSET 0;

-- 使用 MySQL FULLTEXT 查询文章关键词。
SELECT id, title, slug, summary, published_at,
       MATCH(title, summary, content_md) AGAINST ('Spring Boot' IN NATURAL LANGUAGE MODE) AS score
FROM articles
WHERE status = 'published'
  AND deleted_at IS NULL
  AND MATCH(title, summary, content_md) AGAINST ('Spring Boot' IN NATURAL LANGUAGE MODE)
ORDER BY score DESC, published_at DESC
LIMIT 20 OFFSET 0;

-- 查询文章一级评论列表。
SELECT id, article_id, user_id, content, status, created_at
FROM comments
WHERE article_id = 1
  AND parent_id IS NULL
  AND status = 'published'
  AND deleted_at IS NULL
ORDER BY created_at ASC;
```
