INSERT INTO roles (code, name, description)
VALUES ('USER', '普通用户', '可浏览文章、发表评论')
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  description = VALUES(description),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO roles (code, name, description)
VALUES ('AUTHOR', '作者', '可创建、编辑、发布自己的文章')
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  description = VALUES(description),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO roles (code, name, description)
VALUES ('ADMIN', '管理员', '可管理全站用户、文章、分类、标签和评论')
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  description = VALUES(description),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO users (username, email, phone, password_hash, nickname, avatar_url, bio, status)
VALUES (
  'demo_author',
  'demo_author@example.com',
  '13800009999',
  '$2a$10$SIsNkq59VtNmFn5FJ49id.SMZK1gGIaJspqw.q6wJ4J3sWmtoU0um',
  '示例作者',
  'https://example.com/demo-author.png',
  '用于初始化内容浏览示例文章的作者账号',
  'active'
)
ON DUPLICATE KEY UPDATE
  nickname = VALUES(nickname),
  avatar_url = VALUES(avatar_url),
  bio = VALUES(bio),
  status = VALUES(status),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.code = 'AUTHOR'
WHERE u.username = 'demo_author'
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.code = 'ADMIN'
WHERE u.username = 'demo_author'
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id);

INSERT INTO users (username, email, phone, password_hash, nickname, avatar_url, bio, status)
VALUES (
  'demo_reader',
  'demo_reader@example.com',
  '13800008888',
  '$2a$10$SIsNkq59VtNmFn5FJ49id.SMZK1gGIaJspqw.q6wJ4J3sWmtoU0um',
  '示例读者',
  'https://example.com/demo-reader.png',
  '用于初始化互动功能测试的普通读者账号',
  'active'
)
ON DUPLICATE KEY UPDATE
  nickname = VALUES(nickname),
  avatar_url = VALUES(avatar_url),
  bio = VALUES(bio),
  status = VALUES(status),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.code = 'USER'
WHERE u.username = 'demo_reader'
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id);

INSERT INTO categories (name, slug, description, sort_order, status)
VALUES
  ('技术', 'tech', '技术学习、开发实践、架构设计', 10, 'active'),
  ('产品', 'product', '产品思考、需求分析、项目复盘', 20, 'active'),
  ('生活', 'life', '日常记录、个人成长、随笔', 30, 'active')
ON DUPLICATE KEY UPDATE
  description = VALUES(description),
  sort_order = VALUES(sort_order),
  status = VALUES(status),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO tags (name, slug, status)
VALUES
  ('Spring Boot', 'spring-boot', 'active'),
  ('Kotlin', 'kotlin', 'active'),
  ('产品思考', 'product-thinking', 'active'),
  ('学习记录', 'learning-notes', 'active'),
  ('搜索', 'search', 'active')
ON DUPLICATE KEY UPDATE
  status = VALUES(status),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO articles (
  author_id, title, slug, summary, content_md, content_html, cover_url,
  status, view_count, comment_count, published_at
)
SELECT
  u.id,
  '用 Spring Boot 和 Kotlin 搭建博客 API',
  'spring-boot-kotlin-blog-api',
  '从项目结构、数据库初始化和 Swagger 调试三个角度，梳理一个博客 API 的最小闭环。',
  '# 用 Spring Boot 和 Kotlin 搭建博客 API

这是一篇用于内容浏览接口调试的示例文章。

## 重点

- Spring Boot 负责 Web 服务启动
- JdbcClient 负责数据库访问
- Swagger 用于接口调试',
  '<h1>用 Spring Boot 和 Kotlin 搭建博客 API</h1><p>这是一篇用于内容浏览接口调试的示例文章。</p>',
  'https://example.com/covers/spring-boot-kotlin-blog-api.png',
  'published',
  128,
  0,
  '2026-06-01 10:00:00'
FROM users u
WHERE u.username = 'demo_author'
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  summary = VALUES(summary),
  content_md = VALUES(content_md),
  content_html = VALUES(content_html),
  cover_url = VALUES(cover_url),
  status = VALUES(status),
  view_count = VALUES(view_count),
  comment_count = VALUES(comment_count),
  published_at = VALUES(published_at),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO articles (
  author_id, title, slug, summary, content_md, content_html, cover_url,
  status, view_count, comment_count, published_at
)
SELECT
  u.id,
  '博客内容搜索功能说明',
  'blog-content-search-feature',
  '介绍博客 MVP 中关键词搜索和搜索结果页的接口设计，覆盖标题、摘要和正文匹配。',
  '# 博客内容搜索功能说明

内容搜索用于帮助读者通过关键词快速找到文章。

## 搜索范围

- 文章标题
- 文章摘要
- Markdown 正文

## 排序

- relevance：按匹配相关度排序
- latest：按发布时间排序
- hot：按阅读量排序',
  '<h1>博客内容搜索功能说明</h1><p>内容搜索用于帮助读者通过关键词快速找到文章。</p>',
  'https://example.com/covers/blog-content-search-feature.png',
  'published',
  64,
  0,
  '2026-06-04 10:00:00'
FROM users u
WHERE u.username = 'demo_author'
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  summary = VALUES(summary),
  content_md = VALUES(content_md),
  content_html = VALUES(content_html),
  cover_url = VALUES(cover_url),
  status = VALUES(status),
  view_count = VALUES(view_count),
  comment_count = VALUES(comment_count),
  published_at = VALUES(published_at),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO articles (
  author_id, title, slug, summary, content_md, content_html, cover_url,
  status, view_count, comment_count, published_at
)
SELECT
  u.id,
  '博客 MVP 的内容浏览设计',
  'blog-mvp-content-browsing',
  '说明首页文章列表、文章详情、分类浏览、标签浏览在 MVP 阶段的接口边界。',
  '# 博客 MVP 的内容浏览设计

内容浏览是博客前台最基础的闭环。

## MVP 范围

- 首页文章列表
- 文章详情
- 分类浏览
- 标签浏览',
  '<h1>博客 MVP 的内容浏览设计</h1><p>内容浏览是博客前台最基础的闭环。</p>',
  'https://example.com/covers/blog-mvp-content-browsing.png',
  'published',
  86,
  0,
  '2026-06-02 10:00:00'
FROM users u
WHERE u.username = 'demo_author'
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  summary = VALUES(summary),
  content_md = VALUES(content_md),
  content_html = VALUES(content_html),
  cover_url = VALUES(cover_url),
  status = VALUES(status),
  view_count = VALUES(view_count),
  comment_count = VALUES(comment_count),
  published_at = VALUES(published_at),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO articles (
  author_id, title, slug, summary, content_md, content_html, cover_url,
  status, view_count, comment_count, published_at
)
SELECT
  u.id,
  '持续学习 Web 开发的记录方法',
  'learning-web-development-notes',
  '记录如何把学习过程沉淀成文章、分类和标签，方便后续检索与复盘。',
  '# 持续学习 Web 开发的记录方法

把学习过程写成文章，可以帮助自己复盘。

## 建议

- 按主题分类
- 用标签补充关键词
- 定期归档回顾',
  '<h1>持续学习 Web 开发的记录方法</h1><p>把学习过程写成文章，可以帮助自己复盘。</p>',
  'https://example.com/covers/learning-web-development-notes.png',
  'published',
  42,
  0,
  '2026-06-03 10:00:00'
FROM users u
WHERE u.username = 'demo_author'
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  summary = VALUES(summary),
  content_md = VALUES(content_md),
  content_html = VALUES(content_html),
  cover_url = VALUES(cover_url),
  status = VALUES(status),
  view_count = VALUES(view_count),
  comment_count = VALUES(comment_count),
  published_at = VALUES(published_at),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO article_categories (article_id, category_id)
SELECT a.id, c.id
FROM articles a
JOIN categories c ON c.slug = 'tech'
WHERE a.slug = 'spring-boot-kotlin-blog-api'
ON DUPLICATE KEY UPDATE
  article_id = VALUES(article_id);

INSERT INTO article_categories (article_id, category_id)
SELECT a.id, c.id
FROM articles a
JOIN categories c ON c.slug = 'product'
WHERE a.slug = 'blog-mvp-content-browsing'
ON DUPLICATE KEY UPDATE
  article_id = VALUES(article_id);

INSERT INTO article_categories (article_id, category_id)
SELECT a.id, c.id
FROM articles a
JOIN categories c ON c.slug = 'life'
WHERE a.slug = 'learning-web-development-notes'
ON DUPLICATE KEY UPDATE
  article_id = VALUES(article_id);

INSERT INTO article_categories (article_id, category_id)
SELECT a.id, c.id
FROM articles a
JOIN categories c ON c.slug = 'tech'
WHERE a.slug = 'blog-content-search-feature'
ON DUPLICATE KEY UPDATE
  article_id = VALUES(article_id);

INSERT INTO article_tags (article_id, tag_id)
SELECT a.id, t.id
FROM articles a
JOIN tags t ON t.slug IN ('spring-boot', 'kotlin')
WHERE a.slug = 'spring-boot-kotlin-blog-api'
ON DUPLICATE KEY UPDATE
  article_id = VALUES(article_id);

INSERT INTO article_tags (article_id, tag_id)
SELECT a.id, t.id
FROM articles a
JOIN tags t ON t.slug IN ('product-thinking')
WHERE a.slug = 'blog-mvp-content-browsing'
ON DUPLICATE KEY UPDATE
  article_id = VALUES(article_id);

INSERT INTO article_tags (article_id, tag_id)
SELECT a.id, t.id
FROM articles a
JOIN tags t ON t.slug IN ('learning-notes')
WHERE a.slug = 'learning-web-development-notes'
ON DUPLICATE KEY UPDATE
  article_id = VALUES(article_id);

INSERT INTO article_tags (article_id, tag_id)
SELECT a.id, t.id
FROM articles a
JOIN tags t ON t.slug IN ('search')
WHERE a.slug = 'blog-content-search-feature'
ON DUPLICATE KEY UPDATE
  article_id = VALUES(article_id);
